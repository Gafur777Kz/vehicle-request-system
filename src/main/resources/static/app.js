// токен живёт только в этой переменной: не в localStorage, не в URL и не на странице
let accessToken = null;

const $ = (id) => document.getElementById(id);

// любой запрос к API: свой X-Correlation-Id и Bearer-токен, если мы вошли
async function api(method, path, body) {
    const headers = { 'X-Correlation-Id': crypto.randomUUID() };
    if (body !== undefined) {
        headers['Content-Type'] = 'application/json';
    }
    if (accessToken) {
        headers['Authorization'] = 'Bearer ' + accessToken;
    }
    const response = await fetch(path, {
        method: method,
        headers: headers,
        body: body === undefined ? undefined : JSON.stringify(body)
    });
    let data = null;
    try {
        data = await response.json();
    } catch (e) {
        data = null; // пустое тело
    }
    return { status: response.status, data: data };
}

// показываем статус и статус заявки, а при ошибке - code, message, correlationId
function show(title, result, note) {
    const box = $('result');
    const lines = [title, 'HTTP ' + result.status];
    const d = result.data || {};
    if (result.status < 400) {
        if (d.businessKey) lines.push('Request: ' + d.businessKey + '  id: ' + d.id);
        if (d.status) {
            lines.push('Status: ' + d.status);
            showRoute(d.status);
        }
        box.className = 'result ok';
    } else {
        lines.push('code: ' + d.code);
        lines.push('message: ' + d.message);
        lines.push('correlationId: ' + d.correlationId);
        box.className = 'result error';
    }
    if (note) lines.push(note);
    box.textContent = lines.join('\n');
}

// подсвечиваем маршрут DRAFT -> SUBMITTED -> APPROVED по статусу заявки
const ORDER = ['DRAFT', 'SUBMITTED', 'APPROVED'];

function showRoute(status) {
    const reached = ORDER.indexOf(status);
    document.querySelectorAll('#route li').forEach((li, i) => {
        li.classList.toggle('done', reached >= 0 && i <= reached);
    });
}

function requestId() {
    return $('request-id').value.trim();
}

// вход: POST /api/v1/auth/login, потом GET /api/me чтобы показать имя и роль
$('login-form').addEventListener('submit', async (event) => {
    event.preventDefault();
    const login = await api('POST', '/api/v1/auth/login', {
        username: $('username').value,
        password: $('password').value
    });
    $('password').value = '';
    if (login.status !== 200) {
        show('POST /api/v1/auth/login', login);
        return;
    }
    accessToken = login.data.accessToken;
    const me = await api('GET', '/api/me');
    $('me-name').textContent = me.data.username;
    $('me-role').textContent = me.data.authorities.map((a) => a.replace('ROLE_', '')).join(', ');
    $('me-expires').textContent = (login.data.expiresIn / 60) + ' minutes';
    $('login-card').classList.add('hidden');
    $('session-card').classList.remove('hidden');
    show('POST /api/v1/auth/login', { status: 200, data: {} }, 'Logged in as ' + me.data.username);
});

// выход: просто выбрасываем токен. Следующий вызов API будет 401
$('logout').addEventListener('click', () => {
    accessToken = null;
    $('session-card').classList.add('hidden');
    $('login-card').classList.remove('hidden');
    $('result').className = 'result';
    $('result').textContent = 'Logged out. The next API call should be 401.';
});

$('create-form').addEventListener('submit', async (event) => {
    event.preventDefault();
    const result = await api('POST', '/api/v1/vehicle-requests', {
        businessKey: $('business-key').value,
        purpose: $('purpose').value
    });
    if (result.status === 201) {
        $('request-id').value = result.data.id;
    }
    show('POST /api/v1/vehicle-requests', result);
});

$('read').addEventListener('click', async () => {
    const result = await api('GET', '/api/v1/vehicle-requests/' + requestId());
    show('GET /api/v1/vehicle-requests/{id}', result);
});

$('submit').addEventListener('click', async () => {
    const result = await api('POST', '/api/v1/vehicle-requests/' + requestId() + '/submit');
    show('POST /api/v1/vehicle-requests/{id}/submit', result);
});

// закрытое действие. При 403 перечитываем заявку, чтобы показать: статус не изменился
$('approve').addEventListener('click', async () => {
    const result = await api('POST', '/api/v1/vehicle-requests/' + requestId() + '/approve');
    let note = '';
    if (result.status === 403) {
        const current = await api('GET', '/api/v1/vehicle-requests/' + requestId());
        if (current.status === 200) {
            note = 'Request was not changed, status is still ' + current.data.status;
            showRoute(current.data.status);
        }
    }
    show('POST /api/v1/vehicle-requests/{id}/approve', result, note);
});
