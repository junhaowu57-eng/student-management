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
  $original=@{name=$marker;age=18;gender='M';email='before@example.com'}
  $created=CallApi 'create-fixture' POST '/students' $original $admin
  if($created.code -ne 200){throw 'Fixture creation failed'}
  $page=CallApi 'find-fixture' GET ('/students?name='+$marker) $null $admin
  $id=$page.data.records[0].id
  if(!$id){throw 'Fixture ID missing'}
  $updated=@{id=$id;name=$marker+'updated';age=23;gender='F';email='after@example.com'}
  $null=CallApi 'update-all-fields' PUT '/students' $updated $admin
  $read=CallApi 'read-updated' GET ('/students/'+$id) $null $admin
  $match=$true
  foreach($field in @('id','name','age','gender','email')){if($read.data.$field -ne $updated[$field]){$match=$false}}
  Write-Host ('all-fields-persisted='+$match)
  if(!$match){throw 'Update verification failed'}
  $null=CallApi 'repeat-same-update' PUT '/students' $updated $admin
  $invalid=@{id=$id;name='';age=23;gender='F';email='after@example.com'}
  $null=CallApi 'reject-empty-name' PUT '/students' $invalid $admin
  $invalid.name=$marker+'updated';$invalid.age=-1
  $null=CallApi 'reject-negative-age' PUT '/students' $invalid $admin
  $null=CallApi 'reject-unauthenticated-update' PUT '/students' $updated $null
  $login=CallApi 'user-login' POST '/users/login' @{username='lisi';password='123456'} $null
  if($login.data.role -eq 'USER'){$null=CallApi 'reject-user-update' PUT '/students' $updated $login.data.token}
  $read=CallApi 'verify-rejected-updates-no-change' GET ('/students/'+$id) $null $admin
  $match=$true
  foreach($field in @('id','name','age','gender','email')){if($read.data.$field -ne $updated[$field]){$match=$false}}
  Write-Host ('rejected-requests-preserve-data='+$match)
  $null=CallApi 'reject-missing-id' PUT '/students' @{name=$marker;age=18} $admin
  $null=CallApi 'reject-nonexistent-id' PUT '/students' @{id=2147483647;name=$marker;age=18} $admin
} finally {
  $results|ConvertTo-Json -Depth 6|Set-Content -Encoding UTF8 "$PSScriptRoot/update-results.json"
  & 'E:/Java/bin/java.exe' --class-path 'C:/Users/w1785/.m2/repository/com/mysql/mysql-connector-j/9.7.0/mysql-connector-j-9.7.0.jar' "$PSScriptRoot/DbCleanup.java" cleanup $marker
  $client.Dispose()
}
