# Quién llama

**[⬇ Descargar el APK](https://github.com/alecelular/QuienLlama/releases/latest)** (Android 10 o superior). Después de instalarlo, seguí la sección "Puesta en marcha".

[English version](README.en.md)

App de Android que muestra el **nombre del contacto** en las llamadas entrantes aunque la operadora
mande el número en un formato distinto al de la agenda.

**El problema:** en Argentina, Claro envía `+549 + área + número`, mientras que Movistar/Personal envían
`0 + área + número`. Si tus contactos están guardados con `+549`, algunos teléfonos (por ejemplo, Samsung
con firmware de otra región) no los reconocen y muestran solo el número.

**La solución:** en lugar de duplicar los contactos sin prefijo, la app compara los **últimos N dígitos** del
número entrante contra la agenda.

## Cómo funciona

1. Prueba con todos los dígitos del número entrante.
2. Si no hay coincidencia, quita un dígito por la izquierda y repite, hasta el mínimo configurado (por defecto 10).
3. En el primer nivel con coincidencias:
   - un solo contacto → muestra su nombre;
   - varios contactos → cartel ámbar con **todos los nombres** (útil para detectar contactos duplicados).
4. Nunca baja a un nivel más corto si ya encontró coincidencias.

Detalles: un contacto coincide si sus dígitos *terminan* igual que el sufijo probado; se cuenta por contacto
(dos números de la misma persona no son ambigüedad); los números más cortos que el mínimo (147, 911…)
solo coinciden por igualdad exacta. La lógica está en
[`SuffixMatcher.kt`](app/src/main/java/com/quienllama/SuffixMatcher.kt), con tests en
[`SuffixMatcherTest.kt`](app/src/test/java/com/quienllama/SuffixMatcherTest.kt).

## Privacidad

La app **no tiene permiso de Internet** y no guarda ni envía tus contactos: la agenda se lee en memoria
solo para comparar números. Podés comprobarlo en el
[`AndroidManifest.xml`](app/src/main/AndroidManifest.xml).

## Requisitos

- Android 10 o superior.
- Permisos: leer contactos, mostrar sobre otras apps, y ser la "app de identificación de llamadas".

## Compilar

Necesitás JDK 17, Gradle 8.14 o superior y el SDK de Android con la plataforma 36 y las build-tools 35.0.0.

```
echo "sdk.dir=/ruta/a/tu/android-sdk" > local.properties
./gradlew assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`. También se puede abrir la carpeta con Android Studio.
Para preparar una Ubuntu desde cero, ver [COMPILAR.md](COMPILAR.md).

## Puesta en marcha

Abrí la app y tocá los tres botones hasta que los tres queden en ✔. En Samsung, el tercero también está en
Ajustes → Aplicaciones → Elegir apps predeterminadas → App de ID y spam. Después podés usar
"Probar sin llamar" para ver el resultado con cualquier número.

## Limitaciones conocidas

- Probada en un Samsung Galaxy A17 con operadoras Claro y Movistar. Otros fabricantes pueden comportarse distinto.
- El cartel flota sobre la pantalla de llamada (no la reemplaza); su altura se ajusta desde la app.
- Es posible que el marcador del sistema no avise a la app de números que ya reconoce como contactos.

## Licencia

[PolyForm Noncommercial 1.0.0](https://polyformproject.org/licenses/noncommercial/1.0.0): podés usar,
copiar y modificar la app gratis para fines no comerciales. Para venderla o usarla comercialmente
hace falta permiso del autor.

© 2026 Alejandro F. Fernández (@alecelular en X, Instagram y otras redes)

**Contribuciones:** si enviás cambios (pull requests), se entiende que los cedés bajo esta misma licencia
y que el autor puede reutilizarlos, incluso en versiones comerciales.

## Apoyar el proyecto

Si te resultó útil, podés invitarme un café:
[![Invitame un café](https://cdn.cafecito.app/img/buttons/button_1.svg)](https://cafecito.app/rsp148)

---

*Espero que disfruten este proyecto tanto como yo disfruté su
desarrollo.*
