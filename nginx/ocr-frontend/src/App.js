import React, { useState, useEffect } from "react";
import "./App.css";

const base64UrlEncode = (input) => {
  const bytes = input instanceof ArrayBuffer ? new Uint8Array(input) : input;
  let binary = "";
  for (let i = 0; i < bytes.length; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return window.btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
};

const createCodeVerifier = () => {
  const random = new Uint8Array(32);
  window.crypto.getRandomValues(random);
  return base64UrlEncode(random);
};

const createCodeChallenge = async (verifier) => {
  const data = new TextEncoder().encode(verifier);
  const digest = await window.crypto.subtle.digest("SHA-256", data);
  return base64UrlEncode(digest);
};

function App() {
  const [selectedFiles, setSelectedFiles] = useState([]);
  const [status, setStatus] = useState("Pliki nie zostały jeszcze przesłane.");
  const [margin, setMargin] = useState(1);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [showUserPanel, setShowUserPanel] = useState(false);
  const [token, setToken] = useState(null);

  const keycloakBaseUrl = process.env.REACT_APP_KEYCLOAK_BASE_URL;
  const keycloakRealm = process.env.REACT_APP_KEYCLOAK_REALM;
  const keycloakClientId = process.env.REACT_APP_KEYCLOAK_CLIENT_ID;
  const redirectUri = window.location.origin;
  const keycloakAuthUrl = `${keycloakBaseUrl}/realms/${keycloakRealm}/protocol/openid-connect/auth`;
  const keycloakTokenUrl = `${keycloakBaseUrl}/realms/${keycloakRealm}/protocol/openid-connect/token`;

  const buildAuthUrl = async (kcAction) => {
    const codeVerifier = createCodeVerifier();
    sessionStorage.setItem("kc_pkce_verifier", codeVerifier);
    const codeChallenge = await createCodeChallenge(codeVerifier);
    const params = new URLSearchParams({
      client_id: keycloakClientId,
      redirect_uri: redirectUri,
      response_type: "code",
      scope: "openid",
      code_challenge: codeChallenge,
      code_challenge_method: "S256",
    });
    if (kcAction) {
      params.set("kc_action", kcAction);
    }
    return `${keycloakAuthUrl}?${params.toString()}`;
  };

  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get("code");

    if (code) {
      const codeVerifier = sessionStorage.getItem("kc_pkce_verifier");
      exchangeCodeForToken(code, codeVerifier);
    } else {
      const savedToken = localStorage.getItem("kc_token");
      if (savedToken) {
        setToken(savedToken);
        setIsLoggedIn(true);
      }
    }
  }, []);

  const exchangeCodeForToken = async (code, codeVerifier) => {
    if (!codeVerifier) {
      setStatus("Missing PKCE verifier. Please log in again.");
      return;
    }
    try {
      const response = await fetch(keycloakTokenUrl, {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: new URLSearchParams({
          grant_type: "authorization_code",
          client_id: keycloakClientId,
          code: code,
          redirect_uri: window.location.origin,
          code_verifier: codeVerifier,
        }),
      });

      if (response.ok) {
        const data = await response.json();
        setToken(data.access_token);
        localStorage.setItem("kc_token", data.access_token);
        setIsLoggedIn(true);
        window.history.replaceState({}, document.title, window.location.pathname);
        sessionStorage.removeItem("kc_pkce_verifier");
        setStatus("Zalogowano pomyślnie.");
      } else {
        console.error("Failed to exchange code for token");
      }
    } catch (err) {
      console.error("Error during token exchange:", err);
    }
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    const loginUrl = await buildAuthUrl();
    window.location.assign(loginUrl);
  };

  const handleLogout = () => {
    setIsLoggedIn(false);
    setToken(null);
    localStorage.removeItem("kc_token");
    setShowUserPanel(false);
    setStatus("Wylogowano.");
    
    // Opcjonalnie: wylogowanie z Keycloak
    const logoutUrl = `${keycloakBaseUrl}/realms/${keycloakRealm}/protocol/openid-connect/logout?client_id=${keycloakClientId}&post_logout_redirect_uri=${encodeURIComponent(window.location.origin)}`;
    window.location.assign(logoutUrl);
  };

  const handleRegister = async () => {
    const registerUrl = await buildAuthUrl("register");
    window.location.assign(registerUrl);
  };

  const handleFilesChange = (e) => {
    setSelectedFiles(Array.from(e.target.files));
  };

  const handleMarginChange = (value) => {
    let v = Math.min(100, Math.max(0, parseFloat(value) || 0));
    setMargin(v);
  };

  const handleUpload = async () => {
    if (selectedFiles.length === 0) {
      alert("Najpierw wybierz PDF!");
      return;
    }

    setStatus(`Wysyłanie ${selectedFiles.length} pliku(ów)...`);

    const pdfFiles = selectedFiles.filter((f) => f.type === "application/pdf");

    if (pdfFiles.length === 0) {
      setStatus("Nie wybrano żadnego pliku PDF");
      return;
    }

    const formData = new FormData();
    pdfFiles.forEach((f) => {
      formData.append("files", f);
    });
    formData.append("margin", margin);

    try {
      console.log("Sending request to /api/invoice");
      const response = await fetch("/api/invoice", {
        method: "POST",
        headers: {
          ...(token ? { "Authorization": `Bearer ${token}` } : {}),
        },
        body: formData,
      });

      if (!response.ok) {
        const text = await response.text();
        console.error('Server returned error', response.status, text);
        throw new Error("Błąd wysyłania: " + response.status);
      }

      const data = await response.json();
      console.log('Server response JSON', data);

      const requestId = data.requestId;
      if (!requestId) throw new Error('Brak requestId w odpowiedzi');

      setStatus('Pliki wysłane. Oczekiwanie na plik Excel (requestId: ' + requestId + ')...');
      startPolling(requestId);
    } catch (e) {
      console.error(e);
      setStatus('Błąd: ' + e.message);
    }
  };

  const startPolling = (requestId) => {
    const intervalId = setInterval(async () => {
      try {
        console.log(`Polling for ${requestId}...`);
        const response = await fetch(`/api/zip/${requestId}`, {
          headers: {
            ...(token ? { "Authorization": `Bearer ${token}` } : {}),
          }
        });

        if (response.status === 200) {
          clearInterval(intervalId);
          const blob = await response.blob();
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `${requestId}.zip`;
          document.body.appendChild(a);
          a.click();
          a.remove();
          window.URL.revokeObjectURL(url);
          setStatus('Plik pobrany pomyślnie.');
        } else if (response.status === 202 || response.status === 404) {
          // 202 Accepted or 404 Not Found might mean it's still processing
          setStatus('Przetwarzanie... (requestId: ' + requestId + ')');
        } else {
          console.warn('Unexpected status during polling:', response.status);
        }
      } catch (err) {
        console.error('Polling error:', err);
      }
    }, 2000);
  };

  return (
    <>
      <div className="auth-bar">
        {!isLoggedIn ? (
          <>
            <button onClick={handleLogin} className="small-btn">Zaloguj</button>
            <button onClick={handleRegister} className="small-btn outline">Rejestracja</button>
          </>
        ) : (
          <div className="user-info">
            <span>Witaj!</span>
            <button onClick={() => setShowUserPanel(true)} className="small-btn">Panel Użytkownika</button>
            <button onClick={handleLogout} className="small-btn logout">Wyloguj</button>
          </div>
        )}
      </div>

      <div className="container">
        <h1>OCR PDF → Excel</h1>
      <div className="margin-box">
        <label className="margin-label">
          Marża: <span>{margin.toFixed(2)}</span>%
        </label>
        <div className="margin-controls">
          <input
            type="range"
            min="0"
            max="100"
            step="1"
            value={margin}
            onChange={(e) => handleMarginChange(e.target.value)}
          />
        </div>
      </div>

      <input
        type="file"
        id="pdfInput"
        accept="application/pdf"
        multiple
        onChange={handleFilesChange}
        style={{ display: "none" }}
      />
      <label htmlFor="pdfInput">Wybierz PDF(y)</label>
      <div id="filename">
        {selectedFiles.length > 0
          ? selectedFiles.map((f) => f.name).join(", ")
          : "Brak wybranego pliku"}
      </div>

      <button onClick={handleUpload}>Prześlij do OCR</button>
      <div id="status">{status}</div>

      {showUserPanel && (
        <div className="overlay">
          <div className="popup">
            <h2>Panel Użytkownika</h2>
            <div className="placeholder-content">
              <p>Tu znajdą się Twoje ustawienia i historia dokumentów.</p>
              <p>Status: Aktywny</p>
            </div>
            <button onClick={() => setShowUserPanel(false)}>Zamknij</button>
          </div>
        </div>
      )}
      </div>
    </>
  );
}

export default App;
