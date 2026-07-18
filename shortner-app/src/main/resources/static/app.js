document.addEventListener('DOMContentLoaded', () => {
    // DOM Elements
    const shortenForm = document.getElementById('shortenForm');
    const longUrlInput = document.getElementById('longUrlInput');
    const shortenBtn = document.getElementById('shortenBtn');
    const btnText = shortenBtn.querySelector('.btn-text');
    const loader = shortenBtn.querySelector('.loader');
    const formError = document.getElementById('formError');
    const resultCard = document.getElementById('resultCard');
    const resultShortUrl = document.getElementById('resultShortUrl');
    const resultLongUrl = document.getElementById('resultLongUrl');
    const copyResultBtn = document.getElementById('copyResultBtn');
    const linksGrid = document.getElementById('linksGrid');
    const emptyState = document.getElementById('emptyState');
    const linksLoading = document.getElementById('linksLoading');
    const refreshBtn = document.getElementById('refreshBtn');
    const linkCardTemplate = document.getElementById('linkCardTemplate');

    // State
    let userId = localStorage.getItem('url_shortener_user_id');
    if (!userId) {
        userId = 'user_' + Math.random().toString(36).substring(2, 11) + '_' + Date.now();
        localStorage.setItem('url_shortener_user_id', userId);
    }
    
    // Display shortened user ID removed

    // Initialize
    fetchUrls();

    // Event Listeners
    shortenForm.addEventListener('submit', handleShorten);
    copyResultBtn.addEventListener('click', () => copyToClipboard(resultShortUrl.textContent));
    refreshBtn.addEventListener('click', fetchUrls);

    // Core Functions
    async function handleShorten(e) {
        e.preventDefault();
        
        const longUrl = longUrlInput.value.trim();
        if (!longUrl) return;

        // Reset state
        formError.textContent = '';
        resultCard.classList.add('hidden');
        setLoading(true);

        try {
            // Generate idempotency key
            const idempotencyKey = btoa(longUrl + userId).substring(0, 20);

            const response = await fetch('/api/v1/shorten', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-User-Id': userId
                },
                body: JSON.stringify({
                    url: longUrl,
                    idempotencyKey: idempotencyKey
                })
            });

            const data = await response.json();

            if (!response.ok) {
                if (response.status === 429) {
                    throw new Error('Too many requests. Please try again later.');
                }
                throw new Error(data.message || 'Failed to shorten URL');
            }

            // Display success
            resultShortUrl.textContent = data.shortUrl;
            resultShortUrl.href = data.shortUrl;
            resultLongUrl.textContent = longUrl;
            resultCard.classList.remove('hidden');
            
            showToast('URL shortened successfully!', 'success');
            
            // Refresh dashboard
            fetchUrls();
            
            // Clear input
            longUrlInput.value = '';

        } catch (error) {
            formError.textContent = error.message;
            showToast('Failed to shorten URL', 'error');
        } finally {
            setLoading(false);
        }
    }

    async function fetchUrls() {
        linksLoading.classList.remove('hidden');
        linksGrid.innerHTML = '';
        emptyState.classList.add('hidden');

        try {
            const response = await fetch(`/api/v1/urls?userId=${userId}&page=0&size=50`);
            
            if (!response.ok) {
                throw new Error('Failed to fetch URLs');
            }

            const pageData = await response.json();
            const urls = pageData.content || [];

            if (urls.length === 0) {
                emptyState.classList.remove('hidden');
            } else {
                urls.forEach(urlData => {
                    const card = createUrlCard(urlData);
                    linksGrid.appendChild(card);
                    // Fetch click stats asynchronously for each card
                    fetchClickStats(urlData.shortCode, card);
                });
            }
        } catch (error) {
            console.error(error);
            showToast('Failed to load your links', 'error');
        } finally {
            linksLoading.classList.add('hidden');
        }
    }

    async function fetchClickStats(shortCode, cardElement) {
        try {
            const response = await fetch(`/api/v1/analytics/${shortCode}`);
            if (response.ok) {
                const data = await response.json();
                const countSpan = cardElement.querySelector('.click-count');
                countSpan.textContent = data.totalClicks;
            }
        } catch (error) {
            console.error(`Failed to fetch stats for ${shortCode}`, error);
        }
    }

    async function deleteUrl(shortCode, cardElement) {
        if (!confirm('Are you sure you want to delete this link?')) return;

        try {
            const response = await fetch(`/api/v1/urls/${shortCode}`, {
                method: 'DELETE',
                headers: {
                    'X-User-Id': userId
                }
            });

            if (response.ok) {
                cardElement.remove();
                showToast('Link deleted successfully', 'success');
                if (linksGrid.children.length === 0) {
                    emptyState.classList.remove('hidden');
                }
            } else {
                throw new Error('Failed to delete');
            }
        } catch (error) {
            showToast('Failed to delete link', 'error');
        }
    }

    // Utility Functions
    function createUrlCard(urlData) {
        const template = linkCardTemplate.content.cloneNode(true);
        const card = template.querySelector('.link-card');
        
        // Date formatting
        const date = new Date(urlData.createdAt);
        const dateStr = date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
        
        // Populate data
        card.querySelector('.card-date').textContent = dateStr;
        card.querySelector('.card-long-url').textContent = urlData.longUrl;
        
        // Short URL resolution based on current domain
        const shortUrlFull = window.location.origin + '/' + urlData.shortCode;
        const shortUrlEl = card.querySelector('.card-short-url');
        shortUrlEl.textContent = '/' + urlData.shortCode;
        shortUrlEl.href = shortUrlFull;

        // Attach events
        const deleteBtn = card.querySelector('.delete-btn');
        deleteBtn.addEventListener('click', () => deleteUrl(urlData.shortCode, card));

        const copyBtn = card.querySelector('.copy-btn');
        copyBtn.addEventListener('click', () => copyToClipboard(shortUrlFull));

        return card;
    }

    function setLoading(isLoading) {
        longUrlInput.disabled = isLoading;
        shortenBtn.disabled = isLoading;
        if (isLoading) {
            btnText.classList.add('hidden');
            loader.classList.remove('hidden');
        } else {
            btnText.classList.remove('hidden');
            loader.classList.add('hidden');
        }
    }

    async function copyToClipboard(text) {
        try {
            await navigator.clipboard.writeText(text);
            showToast('Copied to clipboard!', 'success');
        } catch (err) {
            // Fallback
            const textArea = document.createElement('textarea');
            textArea.value = text;
            document.body.appendChild(textArea);
            textArea.select();
            document.execCommand('copy');
            document.body.removeChild(textArea);
            showToast('Copied to clipboard!', 'success');
        }
    }

    function showToast(message, type = 'success') {
        const toastContainer = document.getElementById('toastContainer');
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.textContent = message;
        
        toastContainer.appendChild(toast);
        
        // Remove after animation completes
        setTimeout(() => {
            toast.remove();
        }, 3000);
    }
});
