import React, { useState } from "react";
import "./App.css";

function App() {
  const [selectedFiles, setSelectedFiles] = useState([]);
  const [status, setStatus] = useState("Pliki nie zostały jeszcze przesłane.");
  const [margin, setMargin] = useState(1);

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
      console.log("Sending request to /invoice/");
      const response = await fetch("/invoice", {
        method: "POST",
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
        const response = await fetch(`/zip/${requestId}`);

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
    </div>
  );
}

export default App;
