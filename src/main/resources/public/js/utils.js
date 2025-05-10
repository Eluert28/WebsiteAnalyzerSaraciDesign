/**
 * Website Analyzer - Utility-Funktionen
 */

// API-Basis-URL
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

    try {
        const response = await fetch(url, options);
        console.log(`Status-Code: ${response.status}`);

        // Extrahiere den Content-Type
        const contentType = response.headers.get('Content-Type');
        console.log(`Content-Type: ${contentType}`);

        // Bei Download-Anfragen die Blob-Antwort zurückgeben
        if (contentType && contentType.includes('application/pdf')) {
            return await response.blob();
        }

        // Bei JSON-Antworten
        if (contentType && contentType.includes('application/json')) {
            const responseData = await response.json();

            if (!response.ok) {
                throw new Error(responseData.error || 'Ein Fehler ist aufgetreten');
            }

            return responseData;
        }

        // Falls keine JSON-Antwort
        const text = await response.text();
        console.error('Unerwartete Antwort:', text.substring(0, 100)); // ersten 100 Zeichen der Antwort anzeigen
        throw new Error(`Unerwartetes Antwortformat: ${contentType}`);
    } catch (error) {
        console.error('API-Fehler:', error);
        throw error;
    }
}

/**
 * Zeigt eine Alert-Meldung an
 * @param {string} message - Meldungstext
 * @param {string} type - Typ der Meldung (success, warning, error)
 * @param {string} containerId - ID des Container-Elements
 */
function showAlert(message, type = 'error', containerId = 'alertContainer') {
    const container = document.getElementById(containerId);
    if (!container) return;

    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.textContent = message;

    // Alert oben im Container einfügen
    container.insertBefore(alertDiv, container.firstChild);

    // Alert nach 5 Sekunden ausblenden
    setTimeout(() => {
        alertDiv.style.opacity = '0';
        alertDiv.style.transition = 'opacity 0.5s';

        // Element nach Ausblenden entfernen
        setTimeout(() => {
            alertDiv.remove();
        }, 500);
    }, 5000);
}

/**
 * Formatiert ein Datum in ein lesbares Format
 * @param {string} dateString - Datum als String
 * @returns {string} - Formatiertes Datum
 */
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('de-DE', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

/**
 * Kürzt eine URL, wenn sie zu lang ist
 * @param {string} url - Die zu kürzende URL
 * @param {number} maxLength - Maximale Länge
 * @returns {string} - Gekürzte URL
 */
function truncateUrl(url, maxLength = 40) {
    if (!url) return '';

    // Protokoll entfernen
    let cleanUrl = url.replace(/^https?:\/\//, '');

    // URL kürzen, wenn nötig
    if (cleanUrl.length > maxLength) {
        return cleanUrl.substring(0, maxLength - 3) + '...';
    }

    return cleanUrl;
}

/**
 * Bestimmt die CSS-Klasse für einen Score-Wert
 * @param {number} score - Score-Wert (0-100)
 * @returns {string} - CSS-Klasse (good, average, poor)
 */
function getScoreClass(score) {
    if (isNaN(score) || score === null) return '';
    if (score >= 70) return 'good';
    if (score >= 50) return 'average';
    return 'poor';
}

/**
 * Erstellt ein Score-Element mit Kreis-Visualisierung
 * @param {number} score - Score-Wert (0-100)
 * @param {string} label - Label für den Score
 * @returns {HTMLElement} - Score-Element
 */
function createScoreElement(score, label) {
    const isNumeric = !isNaN(score) && score !== null;

    const container = document.createElement('div');
    container.className = 'score-container';

    const circle = document.createElement('div');
    circle.className = 'score-circle';

    // Fortschritt im Kreis darstellen
    if (isNumeric) {
        circle.style.setProperty('--progress', `${score}%`);

        // Farbe basierend auf Score
        let color = '#ff3333'; // rot (poor)
        if (score >= 70) color = '#00d26a'; // grün (good)
        else if (score >= 50) color = '#ffbb00'; // gelb (average)

        circle.style.borderColor = `${color} ${color} transparent transparent`;

        // Rotation basierend auf Score
        const rotation = (score / 100) * 360;
        circle.style.transform = `rotate(${rotation}deg)`;
    }

    const scoreValue = document.createElement('div');
    scoreValue.className = 'score-value';
    scoreValue.textContent = isNumeric ? score : 'N/A';

    const scoreLabel = document.createElement('div');
    scoreLabel.className = 'score-label';
    scoreLabel.textContent = label;

    circle.appendChild(scoreValue);
    container.appendChild(circle);
    container.appendChild(scoreLabel);

    return container;
}

/**
 * Zeigt einen Ladeindikator an oder blendet ihn aus
 * @param {boolean} show - True zum Anzeigen, False zum Ausblenden
 * @param {string} containerId - ID des Container-Elements
 */
function toggleLoader(show, containerId = 'loader') {
    const loader = document.getElementById(containerId);
    if (loader) {
        loader.style.display = show ? 'block' : 'none';
    }
}