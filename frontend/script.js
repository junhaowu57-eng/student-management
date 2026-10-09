async function login(){
    const username=document.getElementById("username").value;
    const password=document.getElementById("password").value;
    const response=await fetch(
        "http://localhost:8080/users/login",
        {
            method:"POST",
            headers:{
                "Content-Type":"application/json"
            },
            body:JSON.stringify({
                username:username,
                password:password
            })
        }
    );
    const result=await response.json();
    console.log(result);
    if(result.code==200){
        localStorage.setItem(
            "token",
            result.data.token
        );
        localStorage.setItem("role",result.data.role);
        loadStudents();
        alert("登录成功");
    }
}

async function loadStudents() {
    let currentPage=1;
    let totalPages=1;
    const token=localStorage.getItem("token");
    const name=document.getElementById("searchName").value;
    const url="http://localhost:8080/students?page="+currentPage+"&size=10&name="+encodeURIComponent(name);
    const response=await fetch(
        url,
        {
            method:"GET",
            headers:{
                "Authorization":"Bearer "+token
            }
        }
    );
    const result=await response.json();
    totalPages=result.data.totalPages;
    console.log(result);
    const students=result.data.records;
    const tableBody=document.getElementById("studentTableBody");
    tableBody.innerHTML="";
    for(let i=0;i<students.length;i++){
        const role=localStorage.getItem("role");
        if(role=="ADMIN"){
            document.getElementById("addSection").style.display="block";
        }else{
            document.getElementById("addSection").style.display="none";
            document.getElementById("editBox").style.display="none";
        }
        const student=students[i];
        let actionButtons="";
        if(role=="ADMIN"){
            actionButtons="<button onclick='editStudent("+student.id+")'>修改</button>"+
                        "<button onclick='deleteStudent("+student.id+")'>删除</button>";
        }
        tableBody.innerHTML+=
            "<tr>"+
                "<td>"+student.id+"</td>"+
                "<td>"+student.name+"</td>"+
                "<td>"+student.age+"</td>"+
                "<td>"+student.gender+"</td>"+
                "<td>"+student.email+"</td>"+
                "<td>"+actionButtons+"</td>"+
            "</tr>";
    }
}
function previousPage(){
    if(currentPage>1){
        currentPage--;
        loadStudents();
    }
}
function nextPage(){
    if(currentPage<totalPages){
        currentPage++;
        loadStudents();
    }
}
function searchStudents(){
    currentPage=1;
    loadStudents();
}
async function addStudent(){
    alert("点击");
    const token=localStorage.getItem("token");
    const name=document.getElementById("newName").value;
    const age=Number(document.getElementById("newAge").value);
    const gender=document.getElementById("newGender").value;
    const email=document.getElementById("newEmail").value;
    if(!name.trim()||!Number.isInteger(age)||age<1){
        alert("请填写正确的姓名和年龄");
        return;
    }
    const response=await fetch(
        "http://localhost:8080/students",
        {
            method:"POST",
            headers:{
                "Content-Type":"application/json",
                "Authorization":"Bearer "+token
            },
            body: JSON.stringify({
                name:name,
                age:age,
                gender:gender,
                email:email
            })
        }
    );
    alert("收到后端")
    const result=await response.json();
    if(response.ok&&result.code==200){
        alert("新增成功");
        currentPage=1;
        loadStudents();
    }
    else{
        alert(result.message||"新增失败");
    }

}
async function editStudent(id){
    const token=localStorage.getItem("token");
    const response=await fetch(
        "http://localhost:8080/students/"+id,
        {
            method:"GET",
            headers:{
                "Authorization":"Bearer "+token
            }
        }
    );
    const result=await response.json();
    if(!response.ok||result.code!==200){
        alert(result.message||"查询学生失败");
        return;
    }
    const student=result.data;
    document.getElementById("editId").value=student.id;
    document.getElementById("editName").value=student.name;
    document.getElementById("editAge").value=student.age;
    document.getElementById("editGender").value=student.gender;
    document.getElementById("editEmail").value=student.email;
    document.getElementById("editBox").style.display="block";
}
async function saveStudent(){
    const token=localStorage.getItem("token");
    const id=Number(document.getElementById("editId").value);
    const name=document.getElementById("editName").value;
    const age=Number(document.getElementById("editAge").value);
    const gender=document.getElementById("editGender").value;
    const email=document.getElementById("editEmail").value;
    const response=await fetch(
        "http://localhost:8080/students",
        {
            method:"PUT",
            headers:{
                "Content-Type":"application/json",
                "Authorization":"Bearer "+token
            },
            body:JSON.stringify({
                id:id,
                name:name,
                age:age,
                gender:gender,
                email:email
            })
        }
    );
    const result=await response.json();
    if(response.ok&&result.code===200){
        alert("修改成功");
        document.getElementById("editBox").style.display="none";
        loadStudents();
    }
    else{
        alert(result.message||"修改失败");
    }

}
async function deleteStudent(id){
    const confirmed=confirm("确定删除这个学生吗？");
    if(!confirm){
        return;
    }
    const token=localStorage.getItem("token");
    const response=await fetch(
        "http://localhost:8080/students/"+id,
        {
            method:"DELETE",
            headers:{
                "Authorization":"Bearer "+token
            }
        }
    );
    const result=await response.json();
    if(response.ok&&result.code==200){
        alert("删除成功");
        currentPage=1;
        loadStudents();
    }else{
        alert(result.message||"删除失败");
    }
}
function logout(){
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    window.location.reload();
}