# Details

A Fabric login system for local authentication of Minecraft accounts.

In order to use:

- **Register yourself:** `/register <password>` _(on first join)_

- **Login:** `/login <password>` _(on subsequent logins)_

## Security

All data is located under `.minecraft/config/gatekeeper`.

User data is hashed with SHA-256 and salted, then stored as a JSON under `database.json`:

```json
{
  "e5dd22dc-f598-3b10-8140-672a285a563c": {
    "hash": "8b7d15b4bc0bb893976bcf3e15079d9d70c4537cc76e6f1a7d9cc634ae352d1b",
    "salt": "014dadc3f9fde0fefe1933fc64a30634"
  }
}
```

