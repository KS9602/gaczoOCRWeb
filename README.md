Aplikacja w trakcie rozwoju.

GaczoOCRWeb jest aplikacją webową służącą do rozczytywania faktur w formacie pdf, modyfikacji (dodawanie kolumn, wymnażanie wartości) i zapisu pliku w formacie 
.xlsx w paczce zip
Pierwotnie była to aplikacja desktopowa, jednak przerobiłem ją na mikroserwisową aplikację przeglądarkową.
Do napisania gaczoOCRWeb wykorzystałem javę, spring'a (w tym spring security), kafkę, keycolack'a, nginxa, AWS S3, postgres'a oraz react'a 
(prosty frontend napisany z wykorzystaniem junie)

Flow aplikacji:
1. Użytkownik loguje się w aplikacji frontendowej (zostaje przeniesiony do keycloaka) 
2. Po zalogowaniu i uzyskaniu tokenów użytkownik może przeslac plik pdf
3. Request z plikiem trafia przez nginxa do aplikacji apiGateway która weryfikuje użytkownika i przesyla request do storage (spring webflux)
4. Storage ponownie weryfikuje tokeny. Jeśli są odpowiednie rozpoczyna się weryfikacja pliku i zapis w S3. Po zapisie uuid zostaje zwrócony do fronendu,
 oraz opublikowany (wraz z listą plików) w kafce.
5A. Po otrzymaniu komunikatu z kafki mikroserwis ocr podejmuje pracę. Rozczytuje pdfa, wyszukuje klasę ocr odpowiedzialną za dany typ faktur, zczytuje i modyfikuje dane
i zapisuje plik w S3 formacie xlsx (zip)
5B. fronend po otrzymaniu uuid przygotowuje url i rozpoczyna short polling (po zakończeniu kolejnych kroków pod tym adresem będzie dostępny plik .zip)

Uwagi:
* Mikroserwis OCR opiera się na wcześniej przygotowanych klasach ocr dostosowanych pod dany typ faktury. Przy "biznesowym" zastosowaniu aplikacji, należaloby zglośić
potrzebę przygotowania odpowiedniej klasy.
* Najtrudniejszym etapem jest wyciągnięcie pozycji Y danego wiersza oraz jego wysokości. Klasy OCR mają kilka strategii "wyciągania" tych danych, jednak zwykle opiera
się to na sprawdzaniu zmian koloru w danej kolumnie pixel po pixelu. Jest to rozwiązanie podatne na blędy i do przerobienia w przyszlosci (na testowanych fakturach,
metoda ta dziala bez zarzutu)

TODO:
1. Dokończyc autoryzację i autentykację
2. Panel użytkownika, w tym panel do zglaszania potrzeby wykonania ocr
