path "/auth/token/create" {
  capabilities = ["update"]
}

path "global/*" {
  capabilities = [ "read", "list" ]
}

path "secret/ScrumFuPanda/*" {
  capabilities = [ "read", "list" ]
}
