let token = localStorage.getItem('token');
let userRole = localStorage.getItem('role');

// Verifica login no carregamento
window.onload = () => {
    if (token) showDashboard();
}

function login() {
    const u = document.getElementById('username').value;
    const p = document.getElementById('password').value;
    fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: u, password: p })
    }).then(r => r.json()).then(data => {
        if (data.success) {
            token = data.token;
            userRole = data.role;
            localStorage.setItem('token', token);
            localStorage.setItem('role', userRole);
            showDashboard();
        } else {
            document.getElementById('login-error').innerText = "Credenciais inválidas!";
        }
    });
}

function logout() {
    localStorage.clear();
    token = null;
    userRole = null;
    document.getElementById('dashboard-view').style.display = 'none';
    document.getElementById('login-view').style.display = 'block';
    document.getElementById('user-info').style.display = 'none';
}

function showDashboard() {
    document.getElementById('login-view').style.display = 'none';
    document.getElementById('dashboard-view').style.display = 'block';
    document.getElementById('user-info').style.display = 'block';
    
    // Mostra/Oculta botão de novo chamado baseado na role
    if(userRole !== 'FUNCIONARIO' && userRole !== 'CHEFE_TI') {
        document.getElementById('btn-new-ticket').style.display = 'none';
    } else {
        document.getElementById('btn-new-ticket').style.display = 'inline-block';
    }

    if(userRole === 'CHEFE_TI') {
        document.getElementById('btn-admin').style.display = 'inline-block';
        loadUsers();
    } else {
        document.getElementById('btn-admin').style.display = 'none';
    }

    openTab('tickets-tab');
}

function openTab(tabId) {
    document.querySelectorAll('.tab-content').forEach(el => {
        el.style.display = 'none';
        el.classList.remove('active');
    });
    const target = document.getElementById(tabId);
    target.style.display = 'block';
    
    // Force reflow for transition
    void target.offsetWidth;
    
    target.classList.add('active');
    
    if(tabId === 'tickets-tab') loadTickets();
}

function loadTickets() {
    fetch('/api/tickets', {
        headers: { 'Authorization': 'Bearer ' + token }
    }).then(r => r.json()).then(data => {
        const list = document.getElementById('ticket-list');
        list.innerHTML = '';
        data.forEach(t => {
            let color = 'red'; // VERMELHO
            let statusText = 'Pendente (Não atribuído)';
            let tooltip = '';

            if(t.status === 'LARANJA') {
                color = 'orange';
                statusText = 'Em andamento';
            }
            if(t.status === 'AMARELO') {
                color = 'gold';
                statusText = 'Pendente';
                let creatorName = t.createdBy ? t.createdBy.username : 'desconhecido';
                tooltip = `title="Falta a confirmação do funcionário ${creatorName}"`;
            }
            if(t.status === 'VERDE') {
                color = 'lightgreen';
                statusText = 'Concluído';
            }

            list.innerHTML += `
                <div style="border-left: 5px solid ${color}; padding: 10px; margin-bottom: 10px; background: #222;" ${tooltip}>
                    <h3>[${t.priority}] ${t.title}</h3>
                    <p>${t.description}</p>
                    <small>Status: ${statusText}</small>
                    ${renderTicketActions(t)}
                </div>
            `;
        });
    });
}

function renderTicketActions(t) {
    let html = '<br>';
    if(userRole === 'CHEFE_TI') {
        html += `<button onclick="actionTicket(${t.id}, 'delete')" style='background:red;color:white;margin-right:5px;'>Apagar Chamado</button> `;
    }
    if(userRole === 'DEV' || userRole === 'CHEFE_TI') {
        if(t.status === 'VERMELHO') html += `<button onclick="actionTicket(${t.id}, 'assign')">Assumir Chamado</button> `;
        if(t.status === 'LARANJA') html += `<button onclick="actionTicket(${t.id}, 'complete')">Marcar como Resolvido</button> `;
    }
    if(userRole === 'FUNCIONARIO' && t.status === 'AMARELO') {
        html += `<button onclick="actionTicket(${t.id}, 'confirm')">Confirmar Solução (Finalizar)</button> `;
    }
    return html;
}

function actionTicket(id, action) {
    fetch(`/api/tickets/${id}/${action}`, {
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + token }
    }).then(() => loadTickets());
}

function createTicket() {
    const title = document.getElementById('t-title').value;
    const desc = document.getElementById('t-desc').value;
    fetch('/api/tickets', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
        body: JSON.stringify({ title, description: desc })
    }).then(() => {
        alert("Chamado criado! O Cornelius já definiu a prioridade.");
        document.getElementById('t-title').value = '';
        document.getElementById('t-desc').value = '';
        openTab('tickets-tab');
    });
}

function loadUsers() {
    fetch('/api/users', { headers: { 'Authorization': 'Bearer ' + token } })
    .then(r => r.json()).then(data => {
        const list = document.getElementById('users-list');
        list.innerHTML = '';
        data.forEach(u => {
            list.innerHTML += `
            <div style='border: 1px solid #555; padding: 10px; margin-bottom: 5px; background: #222;'>
                <strong>${u.username}</strong> (${u.role}) 
                <button onclick='deleteUser(${u.id})' style='background: red; color: white; float: right; padding: 5px; cursor:pointer;'>Excluir</button>
            </div>`;
        });
    });
}

function createUser() {
    const u = document.getElementById('new-user-name').value;
    const p = document.getElementById('new-user-pass').value;
    const r = document.getElementById('new-user-role').value;
    fetch('/api/users', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
        body: JSON.stringify({ username: u, password: p, role: r })
    }).then(() => { 
        loadUsers(); 
        alert('Usuário criado'); 
        document.getElementById('new-user-name').value = '';
        document.getElementById('new-user-pass').value = '';
    });
}

function deleteUser(id) {
    fetch('/api/users/' + id, { method: 'DELETE', headers: { 'Authorization': 'Bearer ' + token } }).then(() => loadUsers());
}
