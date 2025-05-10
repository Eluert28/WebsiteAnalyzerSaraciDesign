/**
 * Sendet eine Anfrage an die API
 * @param {string} endpoint - API-Endpunkt (z.B. '/websites')
 * @param {string} method - HTTP-Methode (GET, POST, PUT, DELETE)
 * @param {Object} data - Daten für POST/PUT-Anfragen
 * @returns {Promise} - Promise mit der API-Antwort
 */
async function apiRequest(endpoint, method = 'GET', data = null) {
    const url = API_BASE_URL + endpoint;
    console.log(`Sende ${method}-Anfrage an: ${url}`);

    const options = {
        method,
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json' // Wichtig: Explizit JSON anfordern
        }
    };

    if (data) {
        options.body = JSON.stringify(data);
    }

    try {
        const response = await fetch(url, options);
        console.log(`Status-Code: ${response.status}`);

        // Extrahiere den Content-Type
        const contentType = response.headers.get('Content-Type');
        console.log(`Content-Type: ${contentType}`);

        // Fehler mit detaillierterem Logging behandeln
        if (!response.ok) {
            console.error(`HTTP Fehler: ${response.status} ${response.statusText}`);
            let errorMessage = `HTTP Fehler: ${response.status}`;

            // Versuche, Fehlermeldung aus der Antwort zu extrahieren
            try {
                // Prüfe den Content-Type
                if (contentType && contentType.includes('application/json')) {
                    const errorData = await response.json();
                    errorMessage = errorData.error || errorMessage;
                } else {
                    // Bei HTML-Antwort (z.B. 500-Fehler) den Text anzeigen
                    const errorText = await response.text();
                    console.error("Server returned HTML instead of JSON:", errorText.substring(0, 500)); // Log ersten 500 Zeichen
                    errorMessage = "Server-Fehler: Der Server hat eine ungültige Antwort gesendet. Bitte prüfen Sie das Server-Log.";
                }
            } catch (parseError) {
                console.error("Fehler beim Parsen der Fehlerantwort:", parseError);
            }

            throw new Error(errorMessage);
        }

        // Bei Download-Anfragen die Blob-Antwort zurückgeben
        if (contentType && contentType.includes('application/pdf')) {
            return await response.blob();
        }

        // Bei JSON-Antworten
        if (contentType && contentType.includes('application/json')) {
            return await response.json();
        }

        // Falls keine JSON-Antwort
        const text = await response.text();
        console.error('Unerwartete Antwort:', text.substring(0, 500)); // ersten 500 Zeichen der Antwort anzeigen
        throw new Error(`Unerwartetes Antwortformat: ${contentType}`);
    } catch (error) {
        console.error('API-Fehler:', error);
        throw error;
    }
}