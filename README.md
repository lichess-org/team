```mermaid
sequenceDiagram
    invite->>+lichess.org: am I a Lichess team member?
    lichess.org->>+invite: yes, you are
    invite->>+Authentik: request an invitation
    Authentik->>+invite: single-use invite link
    invite->>+Authentik: follow invite link to register
```

## Usage

```bash
sbt app/run
```

http://localhost:8080/

```bash
### or with custom port and Lichess host:

PORT=8000 \
LICHESS_HOST=http://localhost:8080 \
AUTHENTIK_HOST=http://localhost:9000 \
AUTHENTIK_TOKEN=token \
sbt app/run
```

### Development

```bash
sbt scalafmt

sbt scalafix

yamlfmt .github
```

### Docker

To test the Docker image locally:

```bash
sbt Docker/publishLocal

docker run --rm -p 8000:8080 lichess-invite
```
