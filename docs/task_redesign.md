# Редизайн — статус задач

## ✅ Сделано

### Инфраструктура
- `gradle/libs.versions.toml` — добавлены зависимости: `flowmvi = "3.1.0"`, `navigation3 = "1.0.0-alpha02"`, `paging = "3.3.2"`; убраны старые paging-записи; добавлены все соответствующие library-записи
- `settings.gradle.kts` — добавлен `:features:wardrobe`
- `core/database/build.gradle.kts` — исправлена ссылка `libs.androidx.paging.common` → `libs.paging.common`

### core/compose
- `core/compose/build.gradle.kts` — добавлены `libs.flowmvi.core`, `libs.flowmvi.compose`
- `core/compose/.../foundation/Outcome.kt` — создан базовый sealed class `Outcome<T>` (Success / Empty / Error)
- `core/compose/.../foundation/SideEffect.kt` — создан кастомный `SideEffect<T>` wrapper над `MutableSharedFlow`

### Документация
- `docs/design_change/12_paging.md` — создана документация по Paging KMP
- `docs/design_change/11_architecture_flowmvi.md` — обновлено: Outcome<T>, UseCase-split (Query/Command), Container→ViewModel, factory→single, @SerialName везде
- `docs/design_change/01–07_*.md` — добавлены @SerialName на все DTO, UseCase-паттерн обновлён

### features/wardrobe (структура создана)
- `build.gradle.kts` — KMP-модуль с FlowMVI, Ktor, Coil, Koin, moko-resources
- **Domain**
  - `model/Clothe.kt` — расширенная модель с category, styles, season, color, size, tags, marketplaceLinks
  - `model/FilterOptions.kt` — фильтры + SortOrder
  - `repository/WardrobeRepository.kt` — интерфейс (getClothes, deleteClothe, loadClothe, uploadFromUrl)
  - `usecase/GetClothesUseCase.kt` — `Flow<Outcome<List<Clothe>>>`
  - `usecase/DeleteClotheUseCase.kt` — `suspend Result<Unit>`
  - `usecase/GetClotheByIdUseCase.kt` — `Flow<Outcome<Clothe>>`
- **Data**
  - `dto/ClotheDto.kt` — @Serializable + @SerialName на всех полях
  - `mapper/ClotheMapper.kt` — `ClotheDto.toClothe()` с fallback на `storeUrl`
  - `api/WardrobeApi.kt` — Ktor-клиент (get/upload/uploadFromUrl/delete)
  - `repository/WardrobeRepositoryImpl.kt`
- **Presentation**
  - `interactionModel/WardrobeState.kt`, `WardrobeIntent.kt`, `WardrobeSideEffect.kt`
  - `WardrobeStore.kt` — FlowMVI 3.1.0
  - `WardrobeViewModel.kt` — ViewModel + store.start(viewModelScope)
  - `WardrobeScreen.kt` — LazyVerticalGrid 2 колонки, CategoryChips, EmptyState, SkeletonGrid
- **External / DI**
  - `external/WardrobeRoutes.kt` — `@Serializable sealed class` (Main / ItemDetail / ItemEdit)
  - `di/WardrobeModule.kt` — Koin: single для Api/Repository/UseCases, viewModel

---

## 🔲 Осталось сделать

### 1. Фиксы в features/wardrobe/presentation/list ✅

- [x] **`WardrobeScreen.kt`** — заменить `store.states.collectAsState()` на `store.state.collectAsState()`
- [x] **`WardrobeStore.kt`** — в блоке `init` заменить `intent(...)` на `send(...)`; тип возврата `Store` → `ImmutableStore`
- [x] **`WardrobeState.kt`** — добавить поле `isFilterActive: Boolean = false`
- [x] **`WardrobeIntent.kt`** — уже был `sealed interface : MVIIntent` (не требовал изменений)
- [x] **`WardrobeScreen.kt`** — добавлены Preview: `WardrobeScreenPreview`, `WardrobeScreenEmptyPreview`, `WardrobeScreenLoadingPreview`, `ClotheItemCardPreview`
- [x] **`WardrobeScreen.kt`** — в `WardrobeEmptyState` добавлена вторая кнопка "Выбрать из галереи" (OutlinedButton)
- [ ] **`WardrobeViewModel.kt`** — `.start(viewModelScope)` оставлен как есть (уточнить по ImmutableStore API)

### 2. Новые файлы в features/wardrobe

#### Domain (доп. модели и use cases)
- [x] `model/ClotheDetail.kt` — расширенная модель (material, fit, brand, createdAt, marketplaceLinks, tags) — см. `docs/design_change/02_item_screens.md`
- [x] `model/Look.kt` — минимальная модель образа (id, name, imageUrl) для деталки вещи
- [x] `usecase/UpdateClotheUseCase.kt` — Command UseCase, `suspend Result<ClotheDetail>`
- [x] `usecase/GetClotheOutfitsUseCase.kt` — Query UseCase, `Flow<Outcome<List<Look>>>`
- [x] `usecase/GetClotheByIdUseCase.kt` — обновлён: возвращает `Flow<Outcome<ClotheDetail>>`
- [x] `repository/WardrobeRepository.kt` — дополнен: `getClotheById` → `ClotheDetail`, `updateClothe`, `getClotheOutfits`

#### Data (доп. DTO и mapper)
- [x] `dto/ClotheUpdateRequest.kt` — `@Serializable data class` для PATCH запроса
- [x] `dto/LookDto.kt` — `@Serializable data class` для ответа API
- [x] `mapper/ClotheMapper.kt` — добавлены `toClotheDetail()`, `toUpdateRequest()`, `toLook()`
- [x] `repository/WardrobeRepositoryImpl.kt` — реализованы `getClotheById`, `updateClothe`, `getClotheOutfits`
- [x] `api/WardrobeApi.kt` — добавлены `updateClothe(id, body)`, `getClotheOutfits(clotheId)`

#### Presentation — FilterBottomSheet (внутренний)
- [x] `list/FilterBottomSheet.kt` — секции: Сортировка (radio chips), Сезон (multi-select), Цвет (кружки); кнопки "Сбросить" / "Показать"

#### Presentation — AddBottomSheet (внутренний)
- [x] `list/AddBottomSheet.kt` — три варианта: "Сфотографировать" / "Добавить из галереи" / "Конструктор образов"

#### Presentation — ItemDetailScreen
- [x] `detail/itemDetail/interactionModel/ItemDetailState.kt`
- [x] `detail/itemDetail/interactionModel/ItemDetailIntent.kt`
- [x] `detail/itemDetail/interactionModel/ItemDetailSideEffect.kt`
- [x] `detail/itemDetail/ItemDetailStore.kt`
- [x] `detail/itemDetail/ItemDetailViewModel.kt`
- [x] `detail/itemDetail/ItemDetailScreen.kt` — фото 3:4, характеристики, теги, образы, маркетплейс-ссылки, кнопки "Изменить" / "Удалить", диалог подтверждения

#### Presentation — ItemEditScreen
- [x] `detail/itemEdit/interactionModel/ItemEditState.kt`
- [x] `detail/itemEdit/interactionModel/ItemEditIntent.kt`
- [x] `detail/itemEdit/interactionModel/ItemEditSideEffect.kt`
- [x] `detail/itemEdit/ItemEditStore.kt`
- [x] `detail/itemEdit/ItemEditViewModel.kt`
- [x] `detail/itemEdit/ItemEditScreen.kt` — форма редактирования: поля, SizeSelector, список ссылок + "Добавить/Удалить ссылку", кнопка "Сохранить"

#### External
- [x] `external/WardrobeNavGraph.kt` — composable-функция регистрации WardrobeRoutes (вызывается из NavDisplay content-лямбды в шаге [13])

#### DI
- [x] `di/WardrobeModule.kt` — добавлены `UpdateClotheUseCase`, `GetClotheOutfitsUseCase`, `ItemDetailViewModel`, `ItemEditViewModel`

---

### 3. core/compose/components/ — системные компоненты

Создать в `core/compose/src/commonMain/.../components/`:

- [x] `AppTopBar.kt` — шапка: кнопка назад (round 36dp), центрированный заголовок, trailing-иконка; border-bottom
- [x] `AppBottomSheet.kt` — backdrop + шторка с handle; анимация slide from bottom
- [x] `CategoryChips.kt` — горизонтальный `LazyRow` чипсов
- [x] `ItemCard.kt` — карточка вещи: фото 1:1, имя, тег категории; selectionMode с чекбокс-кружком
- [x] `LookCard.kt` — карточка образа: фото 1:1, имя, кнопка `⋯`; locked-state с blur и оверлеем
- [x] `MoreMenu.kt` — выпадающее меню от anchor; `MoreMenuItem(icon, label, isDestructive, action)`
- [x] `TagChip.kt` — маленький чип-тег (`component.tag.background / foreground`, `tiny`)
- [x] `SettingRow.kt` — строка настройки: иконка 36dp, заголовок, подзаголовок, стрелка, разделитель
- [x] `StatItem.kt` — элемент статистики: число + подпись, вертикальный разделитель
- [x] `SkeletonCard.kt` — заглушка загрузки с пульсацией; 2 строки текста под картинкой
- [x] `MarketplaceLinkRow.kt` — строка ссылки маркетплейса; логика определения названия по домену
- [x] `SizeSelector.kt` — горизонтальные toggle-кнопки размеров
- [x] `TabSwitcher.kt` — сегментированный контрол "Одежда / Образы"; rounded container, активная кнопка белая

> `LookCard`, `MoreMenu`, `SettingRow`, `StatItem`, `MarketplaceLinkRow`, `SizeSelector`, `AppBottomSheet` — создаются по мере нужды в шагах [8] и [11].

После выноса компонентов — обновить `WardrobeScreen.kt`: убрать inline-реализации `CategoryChipsRow`, `ClotheItemCard`, `WardrobeSkeletonGrid` и использовать системные компоненты. ✅

---

### 4. features/outfit (новый модуль) ✅

- [x] `settings.gradle.kts` — добавить `:features:outfit`
- [x] `build.gradle.kts` — аналогично wardrobe (FlowMVI, Ktor, Coil, Koin, moko-resources)
- [x] **Domain**: `model/Look.kt`, `repository/OutfitRepository.kt`, `usecase/GetLooksUseCase.kt`, `DeleteLookUseCase.kt`, `ShareLookUseCase.kt`
- [x] **Data**: `dto/LookDto.kt`, `mapper/LookMapper.kt`, `api/OutfitApi.kt`, `repository/OutfitRepositoryImpl.kt`
- [x] **Presentation/list**: `OutfitState.kt`, `OutfitIntent.kt`, `OutfitSideEffect.kt`, `OutfitStore.kt`, `OutfitViewModel.kt`, `OutfitScreen.kt` — сетка 2 колонки, `LookCard`, AI-карточка генерации, пустое состояние
- [x] **External**: `OutfitRoutes.kt`, `OutfitNavGraph.kt`
- [x] **DI**: `OutfitModule.kt`

---

### 5. features/outfit (деталка и конструктор)

- [x] **Presentation/detail/outfitDetail**: `OutfitDetailState.kt`, `OutfitDetailIntent.kt`, `OutfitDetailSideEffect.kt`, `OutfitDetailStore.kt`, `OutfitDetailViewModel.kt`, `OutfitDetailScreen.kt`
  - см. `docs/design_change/04_outfit_detail_screen.md`
- [x] Модуль `features/outfit_constructor` — отдельный Gradle-модуль
  - [x] `settings.gradle.kts` — добавить `:features:outfit_constructor`
  - [x] Полная структура по шаблону фичи
  - [x] см. `docs/design_change/05_outfit_constructor.md`

---

### 6. features/profile (рефакторинг на FlowMVI) ✅

Существующий модуль переписывается по шаблону Clean Architecture + FlowMVI:

- [x] **Presentation/root**: `ProfileState.kt`, `ProfileIntent.kt`, `ProfileSideEffect.kt`, `ProfileStore.kt`, `ProfileViewModel.kt`, `ProfileScreen.kt`
- [x] **Presentation/detail/editProfile**: `EditProfileState.kt`, `EditProfileIntent.kt`, `EditProfileSideEffect.kt`, `EditProfileStore.kt`, `EditProfileViewModel.kt`, `EditProfileScreen.kt`
- [x] **Presentation/detail/sizes**: `SizesState.kt`, `SizesIntent.kt`, `SizesSideEffect.kt`, `SizesStore.kt`, `SizesViewModel.kt`, `SizesScreen.kt`
- [x] **External**: `ProfileRoutes.kt`, `ProfileNavGraph.kt`
- [x] **Data**: `UserDto.kt`, `UserSizesDto.kt`, `UserMapper.kt`, `UserSizesMapper.kt`, `ProfileApi.kt`, `ProfileRepositoryImpl.kt`
- [x] **Domain**: `User.kt`, `UserSizes.kt`, `SizeConversion.kt`, `ProfileRepository.kt`, `GetProfileUseCase.kt`, `UpdateProfileUseCase.kt`, `ProfileLogoutUseCase.kt`, `GetUserSizesUseCase.kt`, `UpdateUserSizesUseCase.kt`
- [x] **DI**: `ProfileModule.kt` (старый `di.kt` очищен)

---

### 7. features/main (новый модуль — оркестратор) ✅

Выносится из `card-feature`. Точка входа приложения.

- [x] `settings.gradle.kts` — добавить `:features:main`
- [x] `build.gradle.kts` — Navigation 3, Koin, FlowMVI, compose
- [x] `MainClothisScreen.kt` — TopBar ("Clothis" + иконка профиля), `TabSwitcher`, WardrobeScreen / OutfitScreen по вкладке
- [x] `AppNavHost.kt` — `NavDisplay` со всеми маршрутами: Auth, Main, WardrobeRoutes.*, OutfitRoutes.*, ProfileRoutes.*
  - Deep link обработка через `LaunchedEffect + DeepLinkManager.pendingDeeplink`
  - Routes реализуют `NavKey`; используется `entryProvider { entry<T> { } }` DSL
- [x] `AppRoutes.kt` — `@Serializable sealed class : NavKey` (Auth, Main)
- [x] `di/MainModule.kt` — Koin: подключение всех модулей фич через `includes()`

---

### 8. core/deeplink (новый модуль) ✅

- [x] `settings.gradle.kts` — добавить `:core:deeplink`
- [x] `build.gradle.kts` — KMP, Navigation 3
- [x] `DeeplinkPostfix.kt` — `enum class` с сегментами пути (ITEM, LOOK, PROFILE, NONE)
- [x] `DeeplinkRoute.kt` — `sealed class` (ItemDetail, LookDetail, Profile, Unknown)
- [x] `DeeplinkPathParser.kt` — парсер пути `clothis://segment/id`
- [x] `IDeeplinkHandler.kt` — интерфейс обработчика
- [x] `DeepLinkManager.kt` — `object` с `MutableStateFlow<String?>`, методы `emit` / `onHandled`
- [x] **Android**: `DeepLinkReceiver.kt` в androidMain

---

### 9. Подключение к существующей навигации ✅

- [x] `App.kt` (composeApp) — использует `AppNavHost()` из `features/main`, `mainModule` в `initKoin()`
- [x] `composeApp/build.gradle.kts` — добавлены `:features:main`, `:core:compose`

---

### 10. Очистка

- [x] `settings.gradle.kts` — legacy-модули перенесены в конец (card-feature, tiktok-feed, wardrobe-feature, recommendations-page-screen, design-system)
- [x] `composeApp/build.gradle.kts` — убрана зависимость от `card-feature`
- [x] `MainActivity.kt` — переведён на `core:deeplink.DeepLinkManager`, убрана зависимость от `card.design_system` и `card.internal.deeplink`
- [x] `ComposeFileProvider.kt` — переведён на app R (path_provider.xml скопирован в composeApp res)
- [x] Физически удалить `features/tiktok-feed`, `features/card-feature`, `features/wardrobe-feature`, `features/recommendations-page-screen`, `design-system`
- [x] Переименовать `composeApp` → `androidApp` (`settings.gradle.kts` + папка)

---

## Порядок выполнения (рекомендуемый)

```
[1] ✅ Фиксы WardrobeScreen + Store + ViewModel (store.state, send, Intent)
[2] ✅ core/compose/components — TagChip, ItemCard, CategoryChips, SkeletonCard, AppTopBar, TabSwitcher
[3] ✅ WardrobeScreen — использовать системные компоненты (убрать inline)
[4] ✅ FilterBottomSheet + AddBottomSheet (wardrobe/list)
[5] ✅ ItemDetailScreen (wardrobe/detail/itemDetail) + доп. domain/data
[6] ✅ ItemEditScreen (wardrobe/detail/itemEdit)
[7] ✅ WardrobeNavGraph.kt + подключить wardrobeModule в initKoin()
[8] ✅ features/outfit (OutfitScreen + OutfitDetailScreen)
[9] ✅ core/compose/components — LookCard, AppBottomSheet, MoreMenu, остальные компоненты
[10] ✅ features/outfit_constructor
[11] ✅ features/profile рефакторинг (FlowMVI + EditProfile + Sizes)
[12] ✅ core/deeplink
[13] ✅ features/main (MainClothisScreen + AppNavHost с Navigation 3)
[14] ✅ Очистка (физическое удаление tiktok-feed, card-feature; rename composeApp → androidApp)
```

---

## Известные баги / особенности

- `store.states` — `@InternalFlowMVIAPI`; правильно: `val state by store.subscribe()` из `pro.respawn.flowmvi.compose.dsl` (см. `docs/design_change/11_architecture_flowmvi.md`)
- `store.send()` не существует; правильно: `store.intent(it)` (из `IntentReceiver`)
- `store.intent()` в `init`-блоке — вероятно нужно `send()` (FlowMVI 3.x публичный API)
- FlowMVI версия `3.1.1` не существует на Maven Central — используем `3.1.0`
- Paging `3.4.2` не существует — используем `3.3.2` (первая стабильная с KMP-поддержкой)
- `@Preview` в `commonMain` — без параметров (`showBackground` и `name` недоступны в KMP)
- Navigation 3 (`1.0.0-alpha02`) — `NavDisplay` в `androidx.navigation3.ui` (не в `runtime`!); routes обязаны реализовывать `NavKey`; использовать `entryProvider { entry<T> { } }` вместо `when`-лямбды; `compileSdk = 36` обязателен (требует `navigationevent:1.0.0-alpha01`)
