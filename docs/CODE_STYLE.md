# Code Style

## Импорты

### Запрет вызова функций через полный путь (fully qualified name)

Все функции и классы должны вызываться только через `import`, а не через полный путь в месте использования.

```kotlin
// ПРАВИЛЬНО
import dev.icerock.moko.resources.compose.painterResource

Icon(painter = painterResource(MR.images.star))
```

```kotlin
// НЕПРАВИЛЬНО
Icon(painter = dev.icerock.moko.resources.compose.painterResource(MR.images.star))
```

Если возникает конфликт имён между двумя импортами, используй `import as` для разрешения:

```kotlin
import dev.icerock.moko.resources.compose.painterResource
import org.jetbrains.compose.resources.painterResource as composePainterResource

// moko-resources
Icon(painter = painterResource(MR.images.star))

// Compose resources
AsyncImage(placeholder = composePainterResource(Res.drawable.placeholder))
```
