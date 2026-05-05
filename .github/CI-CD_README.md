# CI/CD Pipeline - GitHub Actions

Automatización completa de testing, dockerización y despliegue en Render.

## 📋 Pipeline Flow

```
Push to develop/main
        ↓
    RUN TESTS (JUnit)
        ↓
  BUILD DOCKER IMAGE
        ↓
PUSH TO GITHUB REGISTRY
        ↓
    DEPLOY TO RENDER (main only)
        ↓
    VERIFY DEPLOYMENT
```

## ⚙️ Configuración Requerida

### 1. Secrets en GitHub Repository

Ir a: Settings → Secrets and variables → Actions

**Agregar estos secrets**:

| Secret | Valor | Dónde obtenerlo |
|---|---|---|
| `RENDER_SERVICE_ID` | `srv-xxxxx` | Render dashboard → Settings → Service ID |
| `RENDER_API_KEY` | `rnd_xxxxx` | Render dashboard → Account → API Keys |

### 2. Dockerfile (ya existe)
```
✅ /Dockerfile - Multi-stage build Java 21
```

### 3. render.yaml (ya existe)
```
✅ /render.yaml - Configuración Render
```

## 🚀 Triggers

| Evento | Branch | Acción |
|---|---|---|
| `push` | develop | ✅ Test + Build Docker |
| `push` | main | ✅ Test + Build Docker + Deploy Render |
| `pull_request` | any | ✅ Test solo |

## 📊 Jobs

### 1. **test** (5 min)
- Checkout código
- Setup JDK 21
- Ejecutar `./mvnw test` con caché Maven
- Subir resultados como artifacts

**Condición**: Siempre corre

### 2. **build** (10 min)
- Build JAR: `mvnw clean package -DskipTests`
- Setup Docker Buildx
- Login a GitHub Container Registry
- Build + Push imagen Docker
- Tags automáticos: branch, SHA, latest

**Condición**: Solo en `push` (depende de test)

### 3. **deploy** (3 min)
- Trigger deploy en Render via API
- Wait 30 segundos
- Verificar 10 intentos que Swagger UI responda 200

**Condición**: Solo en `push` a `main` (depende de build)

## 🏷️ Docker Tags

Generados automáticamente:

```
ghcr.io/juanes0789/embedded-payments:develop
ghcr.io/juanes0789/embedded-payments:main-sha7ab3f2
ghcr.io/juanes0789/embedded-payments:latest
```

## 📍 Destino Producción

```
URL: https://embedded-payments-1.onrender.com
Trigger: Push a main branch solamente
AutoDeploy en Render: Desactivar (lo hace GitHub Actions)
```

## ✅ Cómo Usar

### 1. Crear branch y hacer push
```bash
git checkout -b feature/xyz
# ... cambios ...
git push origin feature/xyz
```

### 2. Ver progreso en GitHub
```
Repository → Actions → CI/CD Pipeline
```

### 3. Verificar tests
```
Click en workflow → test job → See logs
```

### 4. Verificar build Docker
```
Click en workflow → build job → See logs
```

### 5. Verificar deploy (main only)
```
Click en workflow → deploy job → See logs
```

## 🐳 Registros Container

Imágenes pushadas a GitHub Container Registry:

```
ghcr.io/juanes0789/embedded-payments
```

Ver en: GitHub → Packages → embedded-payments

## 🔧 Troubleshooting

| Error | Solución |
|---|---|
| Test falla | Ver logs en Actions, fix código, push |
| Docker push falla | Verificar secrets GITHUB_TOKEN |
| Deploy falla | Verificar RENDER_SERVICE_ID y RENDER_API_KEY |
| Swagger no responde | Esperar más en paso "verify" o check Render logs |

## 📈 Monitoreo

**GitHub Actions**:
- Repo → Actions → histórico de workflows
- Email notificaciones si falla

**Render**:
- Dashboard → Logs en tiempo real
- Metrics → CPU, memoria, requestsx

## 🔐 Seguridad

✅ Caché Maven en runners GitHub (privado)
✅ Docker image en registro privado (authentication required)
✅ Secrets nunca en logs (GitHub oculta automáticamente)
✅ Deploy solo desde main branch

## 📝 Archivos

```
.github/workflows/ci-cd.yml  → Pipeline definition
Dockerfile                    → Multi-stage build
render.yaml                   → Render config
pom.xml                       → Maven + tests
```

---

**Estado**: ✅ Listo para usar
**Duración aprox**: Test 5min + Build 10min + Deploy 3min = 18min total
