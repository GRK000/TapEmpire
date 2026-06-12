# Tap Empire

Tap Empire es un **idle / incremental game para Android** construido con **Kotlin y Jetpack Compose**.

Empecé el proyecto como un experimento personal para explorar dos cosas al mismo tiempo:

- diseño de economías en juegos idle
- arquitectura moderna Android con Compose

Con el tiempo fue creciendo hasta convertirse en un sistema bastante completo con **mundos, economía escalable, trabajadores, eventos dinámicos, minijuegos jugables y sistemas de progresión complejos**.

La idea siempre ha sido construir algo que se sienta como **un producto real**, no solo una demo técnica.

---

# Historia: «El Pacto de los Fundadores»

El juego tiene una capa narrativa completa (ver [LORE.md](LORE.md)): heredas el garaje
y la primera moneda de **Aurora Vance**, la fundadora traicionada de Vance & Vex, y
compites contra **Magnus Vex** por recuperar el imperio que él le robó. Cada mecánica
está atada al lore — el prestigio es la *Cláusula del Fénix*, las mascotas son quimeras
rescatadas de los laboratorios de VexCorp, los minijuegos son sus desafíos televisados
y los eventos su guerra sucia.

La historia se cuenta en **11 capítulos interactivos que hacen de tutorial**: cada
mecánica se presenta desde dentro de la ficción (con LIA, la IA de Aurora, como guía)
y los capítulos vividos se releen en **La Crónica** (menú Más → 📖).

---

# Gameplay (visión rápida)

El loop principal es el clásico de los idle games, pero con varias capas estratégicas:

Tap → generar ingresos → invertir → automatizar → optimizar → prestigio → escalar


El jugador empieza generando dinero manualmente, pero progresivamente construye un imperio que produce recursos de forma automática.

A partir de ahí entran más sistemas:

- negocios que generan ingresos
- trabajadores que afectan productividad
- contratos y huelgas
- eventos inesperados
- mascotas con rasgos genéticos
- minijuegos para recompensas adicionales
- prestigio para reiniciar progreso con bonus permanentes

---

# Qué hace interesante el proyecto

Este proyecto no busca solo tener muchas features, sino **cómo interactúan entre ellas**.

Algunas decisiones de diseño:

- El sistema económico está conectado con prácticamente todos los sistemas.
- Los eventos pueden alterar producción, trabajadores o economía.
- Los trabajadores tienen estados y sueldos, lo que introduce riesgo si se ignora el sistema laboral.
- Las mascotas influyen indirectamente en la economía mediante bienestar global.

La idea es que el jugador siempre tenga **algo que optimizar**.

---

# Sistemas implementados

Actualmente el juego incluye:

### Economía y progresión
- sistema de **tap con combos y críticos**
- **negocios generadores** con upgrades y escalado
- **hitos de generadores**: cada hito (10, 25, 50, 100…) duplica la producción del negocio
- **compra masiva** (×1 / ×10 / ×MAX) con cálculo de coste acumulado
- **prestigio** con multiplicadores permanentes
- **El Legado de Aurora**: tienda de Renombre con 6 mejoras permanentes que sobreviven al prestigio (tap, producción, críticos, límite offline, capital semilla, frecuencia del cometa)
- **racha diaria** con recompensas crecientes (hasta ×20) y gemas cada 7 días
- ganancias **offline**

### Eventos en vivo
- **Cometa Dorado**: aparece aleatoriamente en pantalla; cazarlo otorga Frenesí (×7 producción), Tap Rush (×10 por tap), lluvia de monedas o gemas
- feedback **háptico** en taps, críticos, compras y cometas
- **autoguardado** cada 30 segundos (además de onPause)

### Contenido
- múltiples **mundos desbloqueables**
- **misiones diarias**
- sistema amplio de **logros**

### Final de juego
- **El Cierre del Pacto**: al desbloquear Nexus Prime se abre el duelo final de valoraciones contra VexCorp; superarla permite cerrar la apuesta, ver el desenlace de la historia y ganar el **Sello del Fundador** (×2 permanente a todo)

### Sistemas jugables
- **8 minijuegos** seleccionados por calidad (ej. Cosmic Billiards, Gravity Slingshot, Boss Battle con núcleo débil móvil, Coin Rain con modo Fiebre)
- **sistema laboral** con trabajadores por rareza, sueldos y huelgas
- **contratos personalizables**
- **eventos dinámicos** con decisiones

### Mascotas
- **avatares vectoriales únicos** por especie (18 diseños con forma, orejas, alas, cola, accesorio y paleta propios)
- sistema de **cría** — los híbridos **combinan visualmente a ambos padres** (cuerpo del dominante + rasgos del otro)
- **rasgos genéticos**
- mutaciones
- impacto en bienestar del imperio

---

# Stack técnico

- **Lenguaje**  
  Kotlin (principal) + Java en algunos sistemas legacy

- **UI**  
  Jetpack Compose + Material 3

- **Arquitectura**  
  ViewModel + StateFlow

- **Persistencia**  
  SharedPreferences serializando estado completo del juego en JSON

- **Build**
    - Android Gradle Plugin 8.x
    - Kotlin 2.x
    - minSdk 26
    - compileSdk 36

---

# Arquitectura

La app sigue un modelo bastante simple pero robusto.

GameState
↓
GameViewModel
↓
UI (Compose)


### GameState
Contiene toda la lógica de dominio del juego:
economía, progresión, eventos, trabajadores, etc.

### GameViewModel
Coordina el loop del juego, timers, guardado de progreso y emite el estado a la UI.

### UI (Jetpack Compose)
Pantallas reactivas que consumen `StateFlow`.

Esto permite mantener **la lógica del juego separada de la UI**, algo importante cuando el número de sistemas empieza a crecer.

---

# Persistencia

El progreso se guarda localmente usando `SharedPreferences`.

Se serializa todo el estado del juego en JSON:

- recursos
- mundos
- negocios
- upgrades
- trabajadores
- mascotas
- eventos
- progreso general

El guardado ocurre en:

- `onPause()`
- `ViewModel.onCleared()`

Además se calculan **ganancias offline** cuando el jugador vuelve a abrir la app.

---

# Internacionalización

El juego soporta actualmente:

- English
- Español
- Català
- Français

Las strings están separadas por locale en:


- `app/src/main/res/values/strings.xml` (EN base)
- `app/src/main/res/values-es/strings.xml`
- `app/src/main/res/values-ca/strings.xml`
- `app/src/main/res/values-fr/strings.xml`

---

# Estructura del proyecto


```text
app/
├─ ui/
│ ├─ screens/
│ ├─ components/
│ └─ viewmodel/
│
├─ GameState.java
├─ MonetizationManager.java
│
└─ ComposeMainActivity.kt
```

---

# Cómo ejecutar

### Requisitos

- Android Studio reciente
- JDK 11
- Android SDK configurado

### Build

Simplemente abrir el proyecto en Android Studio y ejecutar el módulo `app`.

---

# Por qué construí este proyecto

Quería construir algo que combinara:

- diseño de sistemas de juego
- arquitectura Android moderna
- escalabilidad de features

Los idle games parecen simples, pero esconden muchos problemas interesantes:

- balance económico
- progresión a largo plazo
- sistemas interdependientes
- retención del jugador

Tap Empire ha sido mi forma de explorar todo eso en un proyecto real.

---

# Estado del proyecto

El juego es completamente funcional y sigue evolucionando.

La lógica de dominio tiene **tests unitarios JVM** (`app/src/test/`): economía de
generadores e hitos, genética de cría, Legado de Aurora, Pacto, prestigio y formato
de números.

Las próximas áreas de trabajo son:

- audio (efectos y música por mundo)
- analytics
- integración completa de monetización (Google Play Billing)
- cloud save