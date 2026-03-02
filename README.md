```mermaid
sequenceDiagram
    invite.lichess.app->>+lichess.org: am I a Lichess team member?
    lichess.org->>+invite.lichess.app: yes, you are
    invite.lichess.app->>+Authentik: request an invitation
    Authentik->>+invite.lichess.app: single-use invite link
    invite.lichess.app->>+Authentik: follow invite link
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
```

### Docker

To test the Docker image locally:

```bash
sbt Docker/publishLocal
```
