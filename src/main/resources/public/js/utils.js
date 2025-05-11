// API-Basis-URL definieren
const API_BASE_URL = '/api';

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

    // Timeout für die Anfrage
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 120000); // 2 Minuten Timeout
    options.signal = controller.signal;

    try {
        const response = await fetch(url, options);

        // Timeout aufheben
        clearTimeout(timeoutId);

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
        // Timeout aufheben falls noch nicht geschehen
        clearTimeout(timeoutId);

        if (error.name === 'AbortError') {
            console.error('Anfrage abgebrochen wegen Timeout');
            throw new Error('Die Anfrage hat das Zeitlimit überschritten');
        }

        console.error('API-Fehler:', error);
        throw error;
    }
}

/**
 * Formatiert ein Datum für die Anzeige
 * @param {string} dateString - Die zu formatierende Datumszeichenfolge
 * @returns {string} - Das formatierte Datum
 */
function formatDate(dateString) {
    if (!dateString) return '';

    try {
        const date = new Date(dateString);

        // Überprüfen, ob das Datum gültig ist
        if (isNaN(date.getTime())) {
            return 'Ungültiges Datum';
        }

        // Datum im deutschen Format formatieren
        return date.toLocaleString('de-DE', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    } catch (error) {
        console.error('Fehler beim Formatieren des Datums:', error);
        return 'Fehler beim Formatieren';
    }
}

/**
 * Hilfsfunktion zum Anzeigen von Benachrichtigungen
 * @param {string} message - Die anzuzeigende Nachricht
 * @param {string} type - Der Typ der Benachrichtigung ('info', 'success', 'warning', 'error')
 */
function showAlert(message, type = 'info') {
    const alertContainer = document.getElementById('alertContainer');
    if (!alertContainer) return;

    // Vorherige Benachrichtigungen entfernen
    while (alertContainer.firstChild) {
        alertContainer.removeChild(alertContainer.firstChild);
    }

    const alert = document.createElement('div');
    alert.className = `alert alert-${type}`;
    alert.textContent = message;

    alertContainer.appendChild(alert);

    // Nach 8 Sekunden automatisch ausblenden
    setTimeout(() => {
        alert.style.opacity = '0';
        alert.style.transition = 'opacity 0.5s';

        setTimeout(() => {
            if (alertContainer.contains(alert)) {
                alertContainer.removeChild(alert);
            }
        }, 500);
    }, 8000);
}

/**
 * Kürzt eine URL für die Anzeige
 * @param {string} url - Die zu kürzende URL
 * @returns {string} - Die gekürzte URL
 */
function truncateUrl(url) {
    if (!url) return '';

    // HTTP/HTTPS entfernen
    let cleanUrl = url.replace(/^https?:\/\//, '');

    // Wenn URL zu lang ist, kürzen
    if (cleanUrl.length > 35) {
        return cleanUrl.substring(0, 32) + '...';
    }

    return cleanUrl;
}

/**
 * Bestimmt die CSS-Klasse für einen Score
 * @param {number|string} score - Der Score (0-100 oder 'N/A')
 * @returns {string} - Die CSS-Klasse ('success', 'warning', 'error', 'neutral')
 */
function getScoreClass(score) {
    if (score === 'N/A' || isNaN(score)) {
        return 'neutral';
    }

    const numScore = parseInt(score);
    if (numScore >= 80) {
        return 'success';
    } else if (numScore >= 50) {
        return 'warning';
    } else {
        return 'error';
    }
}