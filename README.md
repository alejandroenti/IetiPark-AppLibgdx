
Joc mòbil multijugador inspirat en [Pico Park](https://picoparkgame.com/en/pp1/), desenvolupat amb **LibGDX**.

Els jugadors han de cooperar per superar nivells plens de plataformes i trencaclosques, treballant junts en temps real.

## 🚀 Tecnologies

| Tecnologia | Ús |
|---|---|
| **LibGDX** | Motor del joc (renderitzat i input) |
| **WebSockets** | Comunicació en temps real amb el servidor |
| **Android** | Plataforma mòbil objectiu |

## 🏗️ Arquitectura

```
┌──────────────┐         WebSocket         ┌──────────────┐
│  Client mòbil│ ◄──────────────────────►  │   Servidor   │
│  (LibGDX)    │    JSON / Missatges       │  (Backend)   │
└──────────────┘                           └──────────────┘
```

- El client mòbil gestiona la lògica de joc local, el renderitzat i la interacció de l'usuari.
- La comunicació amb el servidor es realitza via **WebSockets**, permetent sincronització multijugador en temps real.

## 📲 Descarrega

Escaneja el codi QR següent per descarregar l'aplicació al teu dispositiu mòbil:

<p align="center">
  <img src="docs/qr-download.png" alt="QR per descarregar l'app" width="200"/>
</p>

> ⚠️ Assegura't de tenir habilitada la instal·lació d'aplicacions de fonts desconegudes al teu dispositiu Android.

## 🎮 Com jugar

1. Descarrega i instal·la l'aplicació mitjançant el codi QR.
2. Uneix-te a una sala multijugador.
3. Coopera amb els altres jugadors per superar cada nivell.

## 🛠️ Desenvolupament

### Requisits previs

- **Java JDK** 17+
- **Android SDK**
- **Gradle**

### Compilar i executar

```bash
# Clonar el repositori
git clone https://github.com/alejandroenti/IetiPark-AppLibgdx.git
cd IetiPark-AppLibgdx

# Executar en mode escriptori (debug)
./gradlew desktop:run

# Generar APK d'Android
./gradlew android:assembleDebug
```

## Projectes relacionats

- [IetiPark - Web](https://github.com/alejandroenti/IetiPark-AppLibgdx) — Visualitzador de la partida del joc IetiPart fent servir Flutter
- [IetiPark - Server](https://github.com/alejandroenti/IetiPark-AppLibgdx) — Servidor de s'encarrega de la comunicació del joc IetiPark fent servir NodeJS

## 📄 Llicència

Aquest projecte està llicenciat sota la **GNU General Public License v3.0**. Consulta el fitxer [LICENSE](LICENSE) per a més detalls.
