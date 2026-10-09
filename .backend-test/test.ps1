$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.Net.Http
$client=[System.Net.Http.HttpClient]::new()
$client.Timeout=[TimeSpan]::FromSeconds(20)
$results=[System.Collections.Generic.List[object]]::new()
$marker='codextest'+[DateTime]::Now.ToString('yyyyMMddHHmmss')
Set-Content -LiteralPath "$PSScriptRoot/marker.txt" -Value $marker
function CallApi($label,$method,$path,$body,$token){
  $req=[System.Net.Http.HttpRequestMessage]::new([System.Net.Http.HttpMethod]::new($method),'http://localhost:8080'+$path)
  if($token){$req.Headers.TryAddWithoutValidation('Authorization','Bearer '+$token)|Out-Null}
  if($null -ne $body){$json=ConvertTo-Json -InputObject $body -Compress;$req.Content=[System.Net.Http.StringContent]::new($json,[Text.Encoding]::UTF8,'application/json')}
  $res=$client.SendAsync($req).GetAwaiter().GetResult()
  $raw=$res.Content.ReadAsStringAsync().GetAwaiter().GetResult()
  try{$obj=$raw|ConvertFrom-Json}catch{$obj=$null}
  $row=[pscustomobject]@{test=$label;http=[int]$res.StatusCode;code=$obj.code;message=$obj.message}
  $results.Add($row);$row|ConvertTo-Json -Compress|Write-Host
  return $obj
}
function B64($bytes){return [Convert]::ToBase64String($bytes).TrimEnd('=').Replace('+','-').Replace('/','_')}
function TestToken($expired){
  $now=[DateTimeOffset]::UtcNow.ToUnixTimeSeconds();$exp=$now+600;if($expired){$exp=$now-60}
  $h=B64 ([Text.Encoding]::UTF8.GetBytes('{"alg":"HS256"}'))
  $p=B64 ([Text.Encoding]::UTF8.GetBytes((@{sub=$marker;role='ADMIN';iat=$now-120;exp=$exp}|ConvertTo-Json -Compress)))
  $mac=[Security.Cryptography.HMACSHA256]::new([Text.Encoding]::UTF8.GetBytes('student_management_secret_key_student_management_secret_key'))
  return $h+'.'+$p+'.'+(B64 ($mac.ComputeHash([Text.Encoding]::UTF8.GetBytes($h+'.'+$p))))
}
$admin=TestToken $false
try {
  $null=CallApi 'no-token' GET '/students' $null $null
  $null=CallApi 'invalid-token' GET '/students' $null 'invalid'
  $null=CallApi 'expired-token' GET '/students' $null (TestToken $true)
  $null=CallApi 'empty-register' POST '/users/register' @{username='';password=''} $null
  $null=CallApi 'register' POST '/users/register' @{username=$marker;password='TestPwd123!';role='ADMIN'} $null
  $null=CallApi 'duplicate-register' POST '/users/register' @{username=$marker;password='TestPwd123!'} $null
  $null=CallApi 'wrong-password' POST '/users/login' @{username=$marker;password='wrong'} $null
  $login=CallApi 'login' POST '/users/login' @{username=$marker;password='TestPwd123!'} $null
  if(!$login.data.token){throw 'Login failed'}
  Write-Host ('registered-role='+$login.data.role)
  $userToken=$login.data.token
  $null=CallApi 'user-list' GET '/students?page=1&size=10' $null $userToken
  $student=@{name=$marker;age=18;gender=([string][char]0x7537);email='test@example.com'}
  $null=CallApi 'user-create-forbidden' POST '/students' $student $userToken
  $null=CallApi 'admin-create' POST '/students' $student $admin
  $page=CallApi 'name-filter' GET ('/students?name='+$marker) $null $admin
  $id=$page.data.records[0].id
  Write-Host ('filter-count='+$page.data.total)
  $null=CallApi 'detail' GET ('/students/'+$id) $null $admin
  $student.id=$id;$student.age=21
  $null=CallApi 'user-update-forbidden' PUT '/students' $student $userToken
  $null=CallApi 'user-delete-forbidden' DELETE ('/students/'+$id) $null $userToken
  $null=CallApi 'admin-update' PUT '/students' $student $admin
  $detail=CallApi 'verify-update' GET ('/students/'+$id) $null $admin
  Write-Host ('updated-age='+$detail.data.age)
  $null=CallApi 'invalid-page' GET '/students?page=0' $null $admin
  $null=CallApi 'invalid-size' GET '/students?size=101' $null $admin
  $null=CallApi 'invalid-page-type' GET '/students?page=abc' $null $admin
  $null=CallApi 'empty-name' POST '/students' @{name='';age=18} $admin
  $null=CallApi 'negative-age' POST '/students' @{name=$marker+'negative';age=-1} $admin
  $null=CallApi 'missing-age' POST '/students' @{name=$marker+'nullage'} $admin
  $null=CallApi 'invalid-email' POST '/students' @{name=$marker+'email';age=18;email='not-an-email'} $admin
  $null=CallApi 'missing-update-id' PUT '/students' @{name=$marker;age=18} $admin
  $null=CallApi 'missing-record' GET '/students/2147483647' $null $admin
  $null=CallApi 'missing-update-record' PUT '/students' @{id=2147483647;name=$marker;age=18} $admin
  $null=CallApi 'admin-delete' DELETE ('/students/'+$id) $null $admin
  $null=CallApi 'verify-delete' GET ('/students/'+$id) $null $admin
  $null=CallApi 'delete-again' DELETE ('/students/'+$id) $null $admin
} finally {
  $results|ConvertTo-Json -Depth 6|Set-Content -Encoding UTF8 "$PSScriptRoot/results.json"
  & 'E:/Java/bin/java.exe' --class-path 'C:/Users/w1785/.m2/repository/com/mysql/mysql-connector-j/9.7.0/mysql-connector-j-9.7.0.jar' "$PSScriptRoot/DbCleanup.java" cleanup $marker
  $client.Dispose()
}
