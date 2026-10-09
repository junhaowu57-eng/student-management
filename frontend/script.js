let currentPage = 1;
let totalPages = 1;
let sessionVersion = 0;
let listVersion = 0;
let listLoading = false;
let submitting = false;
let editing = false;
let detailLoading = false;
let toastTimer;
const pendingDeletes = new Set();
const $ = id => document.getElementById(id);
const isAdmin = () => !!localStorage.getItem('token') && localStorage.getItem('role') === 'ADMIN';
function notify(message) {
    clearTimeout(toastTimer);
    $('toast').textContent = message;
    $('toast').hidden = false;
    toastTimer = setTimeout(() => { $('toast').hidden = true; }, 3500);
}
function updatePagination() {
    $('pageInfo').textContent = `第 ${currentPage} 页 / 共 ${totalPages} 页`;
    $('previousButton').disabled = listLoading || currentPage <= 1;
    $('nextButton').disabled = listLoading || currentPage >= totalPages;
    $('searchButton').disabled = listLoading;
}
function showManagement() {
    $('loginPage').hidden = true;
    $('managementPage').hidden = false;
    $('currentUsername').textContent = localStorage.getItem('username') || '当前用户';
    $('currentRole').textContent = isAdmin() ? 'ADMIN · 管理员' : 'USER · 普通用户';
    $('addSection').hidden = !isAdmin();
    $('actionHeader').hidden = !isAdmin();
}
function clearSession() {
    sessionVersion++;
    listVersion++;
    for (const key of ['token', 'role', 'username']) localStorage.removeItem(key);
    $('managementPage').hidden = true;
    $('loginPage').hidden = false;
    $('studentTableBody').replaceChildren();
    $('currentUsername').textContent = '';
    $('currentRole').textContent = '';
    $('listStatus').textContent = '';
    $('searchName').value = '';
    $('password').value = '';
    $('addSection').hidden = true;
    $('actionHeader').hidden = true;
    $('studentModal').close();
    $('studentForm').reset();
    $('editId').value = '';
    $('formError').textContent = '';
    $('toast').hidden = true;
    clearTimeout(toastTimer);
    currentPage = totalPages = 1;
    listLoading = false;
    updatePagination();
}
// API paths, verbs, payload fields and Bearer authentication match the existing backend.
async function request(url, options = {}, authenticated = true) {
    const version = sessionVersion;
    const headers = { ...options.headers };
    if (authenticated) {
        const token = localStorage.getItem('token');
        if (!token) throw new Error('请先登录');
        headers.Authorization = 'Bearer ' + token;
    }
    let response;
    try { response = await fetch(url, { ...options, headers }); }
    catch (_) { throw new Error('无法连接服务器，请检查后端是否已启动'); }
    if (version !== sessionVersion) throw new Error('登录状态已变更');
    let result;
    try { result = await response.json(); }
    catch (_) { throw new Error('服务器返回了无法识别的数据'); }
    if (version !== sessionVersion) throw new Error('登录状态已变更');
    if (authenticated && (response.status === 401 || Number(result.code) === 401)) {
        clearSession();
        $('loginError').textContent = '登录已失效，请重新登录';
        throw new Error('登录已失效，请重新登录');
    }
    if (!response.ok || Number(result.code) !== 200) throw new Error(result.message || '操作失败，请稍后重试');
    return result.data;
}
async function login() {
    if ($('loginButton').disabled || !$('loginForm').reportValidity()) return;
    $('loginError').textContent = '';
    $('loginButton').disabled = true;
    $('loginButton').textContent = '正在登录…';
    try {
        const data = await request('http://localhost:8080/users/login', {
            method: 'POST', headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: $('username').value, password: $('password').value })
        }, false);
        if (!data || !data.token) throw new Error('登录返回数据不完整');
        sessionVersion++;
        localStorage.setItem('token', data.token);
        localStorage.setItem('role', data.role);
        localStorage.setItem('username', data.username || $('username').value);
        $('password').value = '';
        currentPage = 1;
        showManagement();
        await loadStudents();
    } catch (error) { $('loginError').textContent = error.message; }
    finally { $('loginButton').disabled = false; $('loginButton').textContent = '登录系统 →'; }
}
function tableMessage(title, description = '') {
    const row = document.createElement('tr');
    const cell = document.createElement('td');
    cell.colSpan = isAdmin() ? 6 : 5;
    cell.className = 'empty-cell';
    const heading = document.createElement('strong');
    heading.textContent = title;
    cell.append(heading, document.createTextNode(description));
    row.append(cell);
    $('studentTableBody').replaceChildren(row);
}
async function loadStudents() {
    if (!localStorage.getItem('token')) return;
    const version = sessionVersion;
    const sequence = ++listVersion;
    listLoading = true;
    updatePagination();
    $('listStatus').textContent = '正在加载…';
    tableMessage('正在加载学生信息', '请稍候');
    try {
        const data = await request('http://localhost:8080/students?page=' + currentPage + '&size=10&name=' + encodeURIComponent($('searchName').value), { method: 'GET' });
        if (sequence !== listVersion || version !== sessionVersion) return;
        if (!data || !Array.isArray(data.records)) throw new Error('学生列表返回格式不正确');
        totalPages = Math.max(1, Number(data.totalPages) || 1);
        if (currentPage > totalPages) { currentPage = totalPages; return await loadStudents(); }
        showManagement();
        $('studentTableBody').replaceChildren();
        for (const student of data.records) {
            const row = document.createElement('tr');
            // Render backend strings as text, never as executable HTML.
            for (const key of ['id', 'name', 'age', 'gender', 'email']) {
                const cell = document.createElement('td');
                cell.textContent = student[key] == null || student[key] === '' ? '—' : String(student[key]);
                row.append(cell);
            }
            if (isAdmin()) {
                const cell = document.createElement('td');
                for (const [label, action, className] of [['修改', editStudent, 'row-action'], ['删除', deleteStudent, 'row-action danger']]) {
                    const button = document.createElement('button');
                    button.textContent = label;
                    button.className = className;
                    button.disabled = submitting || detailLoading || pendingDeletes.has(student.id);
                    button.addEventListener('click', () => action(student.id));
                    cell.append(button);
                }
                row.append(cell);
            }
            $('studentTableBody').append(row);
        }
        if (!data.records.length) tableMessage('暂无学生信息', $('searchName').value ? '试试其他姓名，或重置搜索条件。' : '当前列表为空。');
        $('listStatus').textContent = `本页显示 ${data.records.length} 条学生信息`;
    } catch (error) {
        if (sequence !== listVersion || version !== sessionVersion) return;
        if ($('managementPage').hidden) $('loginError').textContent = error.message;
        tableMessage('加载失败', error.message);
        $('listStatus').textContent = '可点击搜索重新加载';
        totalPages = currentPage;
    } finally {
        if (sequence === listVersion) { listLoading = false; updatePagination(); }
    }
}
function previousPage() { if (!listLoading && currentPage > 1) { currentPage--; loadStudents(); } }
function nextPage() { if (!listLoading && currentPage < totalPages) { currentPage++; loadStudents(); } }
function searchStudents() { currentPage = 1; loadStudents(); }
function resetSearch() { $('searchName').value = ''; searchStudents(); }
function configureModal(edit) {
    editing = edit;
    $('newBox').hidden = edit;
    $('editBox').hidden = !edit;
    for (const input of $('newBox').querySelectorAll('input,select')) input.disabled = edit;
    for (const input of $('editBox').querySelectorAll('input,select')) input.disabled = !edit;
    $('modalTitle').textContent = edit ? '修改学生' : '新增学生';
    $('saveButton').textContent = edit ? '保存修改' : '新增学生';
    $('formError').textContent = '';
    $('studentModal').showModal();
    $(edit ? 'editName' : 'newName').focus();
}
function openAddModal() {
    if (!isAdmin() || submitting || detailLoading) return;
    $('studentForm').reset();
    configureModal(false);
}
function closeModal() {
    if (submitting) return;
    $('studentModal').close();
    $('studentForm').reset();
    $('editId').value = '';
    $('formError').textContent = '';
}
function setActionsBusy(busy) {
    for (const button of document.querySelectorAll('.row-action, #addSection button')) button.disabled = busy;
}
async function editStudent(id) {
    if (!isAdmin() || submitting || detailLoading) return;
    detailLoading = true;
    setActionsBusy(true);
    const version = sessionVersion;
    try {
        const student = await request('http://localhost:8080/students/' + id, { method: 'GET' });
        if (version !== sessionVersion || !isAdmin()) return;
        if (!student) throw new Error('学生信息不存在');
        for (const key of ['id', 'name', 'age', 'gender', 'email']) $('edit' + key[0].toUpperCase() + key.slice(1)).value = student[key] ?? '';
        configureModal(true);
    } catch (error) { if (version === sessionVersion) notify(error.message); }
    finally { detailLoading = false; setActionsBusy(false); }
}
async function submitStudent(edit) {
    if (!isAdmin() || submitting || !$('studentModal').open || !$('studentForm').reportValidity()) return;
    const prefix = edit ? 'edit' : 'new';
    const payload = { name: $(prefix + 'Name').value, age: Number($(prefix + 'Age').value), gender: $(prefix + 'Gender').value, email: $(prefix + 'Email').value };
    if (!payload.name.trim() || !Number.isInteger(payload.age) || payload.age < 1) { $('formError').textContent = '请填写正确的姓名和年龄'; return; }
    if (edit) payload.id = Number($('editId').value);
    const version = sessionVersion;
    submitting = true;
    setActionsBusy(true);
    for (const id of ['saveButton', 'cancelButton', 'closeModalButton']) $(id).disabled = true;
    $('saveButton').textContent = '正在保存…';
    $('formError').textContent = '';
    try {
        await request('http://localhost:8080/students', { method: edit ? 'PUT' : 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
        if (version !== sessionVersion) return;
        $('studentModal').close();
        $('studentForm').reset();
        $('editId').value = '';
        if (!edit) currentPage = 1;
        notify(edit ? '学生信息已更新' : '学生新增成功');
        await loadStudents();
    } catch (error) { if (version === sessionVersion) $('formError').textContent = error.message; }
    finally {
        submitting = false;
        setActionsBusy(false);
        for (const id of ['saveButton', 'cancelButton', 'closeModalButton']) $(id).disabled = false;
        $('saveButton').textContent = edit ? '保存修改' : '新增学生';
    }
}
async function addStudent() { return submitStudent(false); }
async function saveStudent() { return submitStudent(true); }
async function deleteStudent(id) {
    if (!isAdmin() || submitting || pendingDeletes.size || detailLoading) return;
    const confirmed = confirm('确定删除这位学生吗？删除后无法恢复。');
    if (!confirmed) return;
    const version = sessionVersion;
    pendingDeletes.add(id);
    setActionsBusy(true);
    try {
        await request('http://localhost:8080/students/' + id, { method: 'DELETE' });
        if (version !== sessionVersion) return;
        currentPage = 1;
        notify('学生已删除');
        await loadStudents();
    } catch (error) { if (version === sessionVersion) notify(error.message); }
    finally { pendingDeletes.delete(id); setActionsBusy(false); }
}
function logout() { clearSession(); $('loginError').textContent = ''; $('username').focus(); }
$('loginForm').addEventListener('submit', event => { event.preventDefault(); login(); });
$('searchForm').addEventListener('submit', event => { event.preventDefault(); searchStudents(); });
$('studentForm').addEventListener('submit', event => { event.preventDefault(); editing ? saveStudent() : addStudent(); });
$('studentModal').addEventListener('cancel', event => { event.preventDefault(); closeModal(); });
window.addEventListener('storage', event => {
    if (['token', 'role', 'username'].includes(event.key) || event.key === null) {
        clearSession();
        $('loginError').textContent = '登录状态已变更，请重新登录';
    }
});
// Validate an existing token with the backend before revealing the management page.
if (localStorage.getItem('token')) loadStudents();
else updatePagination();
