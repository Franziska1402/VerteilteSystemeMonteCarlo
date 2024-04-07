## Verteilte Berechnung von Pi mithilfe von Monte Carlo - (Geschätzer Aufwand: 13)
- Server schickt Anfrage an mehrere Clients
- Clients berechnen einzeln eine Näherung
- Ergebnis wird zurück an Server geschickt und zusammengerechnet ausgegeben

-> Steuerung über Master Control Program

Workflow:
```
 +-----------------+
 |   Client        |
 +-----------------+
       |  |  Connects to server,
       |  |  Calculate approximation for PI
       |  |
 +-----------+   Sends commands
 |   Server  |<-------------------------------+
 +-----------+                                |
       |  |  Receives commands,               |
       |  |  Sends average result to MCP      |
 +------------------+                         |
 |  Master Control  |<------------------------+
 |     Program      |
 +------------------+
```

