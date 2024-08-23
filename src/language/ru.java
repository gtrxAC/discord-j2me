package language;

public class ru implements Language {
    public ru() {}
    public String[] getStrings() { return strings; }

    static final String[] strings = {
        // Translation notes:
        // - Any line that begins with a "//" is a comment and is ignored.
        //   In this file, comments are used for explaining each string.
        // - Write your translated string inside the two quotation marks.
        // - If you need to use a quotation mark inside any of your translated
        //   strings, write it as \" instead.
        // - If the original string has any special formatting
        //   (e.g. space at the beginning or end), keep the formatting the same.
        // - If you want the English text to be shown for a specific string,
        //   replace the string with null (without any quotation marks).
        // - For any Discord-specific terminology (e.g. servers), use the same
        //   translated term that Discord officially uses for your language.

        // Notes about softkey command labels:
        // - Each softkey label has two variations, a short and a long one.
        // - In this translation file, the short labels are listed above the long ones.
        // - Short labels are used in places where there is limited screen space.
        // - Keep short labels as short as possible, ideally below 10 characters.
        //   If needed, you may use abbreviations.
        // - If a long label is short enough to where it could be used as a
        //   short label, use the same string for both labels.

        // Placeholder file name shown when the name of an attachment in the attachment view screen could not be loaded.
        "Безымянный файл",

        // Title text for the attachment picker (native file picker) screen.
        "Выбрать файл",

        // Softkey labels for going back to the previous screen.
        "Назад",
        "Назад",

        // Softkey label for closing a menu.
        // Currently used in the attachment picker for closing the whole picker (because there is also a "Back" softkey for going back one directory)
        "Закрыть",
        "Закрыть",

        // Title text for attachment view screen.
        "Прикреп. файлы",

        // Softkey labels for refetching contents of channel view or attachment view.
        "Обновить",
        "Обновить",

        // Softkey labels used in channel or DM list for marking the selected channel or DM as read.
        "Пометить как прочит.",
        "Пометить как прочит.",

        // Softkey labels used in channel and DM lists for marking every DM or every channel in the currently shown server as read.
        "Пометить всё как прочит.",
        "Пометить всё как прочит.",

        // Generic "Select" softkey labels.
        "Выбрать",
        "Выбрать",

        // Softkey labels for sending a message in the currently open channel.
        "Отправить",
        "Отправить сообщение",

        // Softkey labels for sending a reply to the selected message.
        "Ответить",
        "Ответить",

        // Softkey labels for sending an attachment in the currently open channel.
        "Загрузить",
        "Загрузить файл",

        // Softkey labels for copying the text content of the selected message.
        "Копировать",
        "Копировать содержимое",

        // Softkey labels for editing the selected message.
        "Изменить",
        "Редактировать",

        // Softkey labels for deleting the selected message.
        "Удалить",
        "Удалить",

        // Softkey labels for selecting an URL in the selected message. This opens a screen where each URL found in the message is listed, and one can be selected to be opened in the device's browser.
        "Открыть URL",
        "Открыть URL адрес",

        // Channel view title suffix when reading older messages.
        " (старые)",

        // Text shown at the center of the channel view when it is empty (no messages).
        "Здесь ничего интересного",

        // Channel view banner text shown when reading older messages and a new message arrives via the gateway.
        "Обновите, чтобы увидеть новые сообщения",

        // Channel view banner text shown when a gateway disconnect occurred and an automatic reconnect is in progress.
        "Повторное подключение",

        // Channel view banner shown when a message is being sent by the client.
        "Отправка сообщения",

        // Channel view banner shown when a message is being edited by the client.
        "Редактирование сообщения",

        // Channel view banner shown when a message is being deleted by the client.
        "Удаление сообщения",

        // Channel view banner shown when messages are being loaded, e.g. during a refresh or after a message was sent by the client.
        "Загрузка сообщений",

        // Suffix for channel view banner text when one person is typing.
        // Example: "aa is typing"
        " печатает",

        // Suffix for channel view banner text when two or three people are typing.
        // Example: "aa, bb, cc are typing"
        " печатают",

        // Suffix for channel view banner text when more than three people are typing.
        // Example: "4 people are typing"
        " человек печатают",

        // Comma separator used to separate usernames in the channel view banner text shown when people are typing.
        ", ",

        // Error messages shown when trying to upload a file, delete a message, or edit a message, and the current proxy server is a direct HTTPS-HTTP proxy (and not a Discord J2ME specific proxy)
        "Этот прокси не поддерживает загрузку файлов", 
        "Этот прокси не поддерживает удаление сообщений",
        "Этот прокси не поддерживает редактирование сообщений",

        // Error message shown when trying to open the native file picker and the device does not support the J2ME FileConnection API.
        "FileConnection не поддерживается",

        // Button labels for navigating between pages of messages in the channel view. Try to keep these as long as (or shorter than) the English strings.
        // In the old channel view, these are shown as softkey labels (these strings are short and long variations of each)
        "Старые",
        "Показать старые сообщения",
        "Новые",
        "Показать новые сообщения",

        // Parts of the button label for viewing attachments of a message.
        // Examples: "View 1 attachment", "View 2 attachments"
        "Посмотреть ",
        " вложение",
        " вложения",

        // Title for confirmation screen shown when deleting a message.
        "Удалить",

        // Body text for confirmation screen shown when deleting a message.
        "Удалить это сообщение?",

        // Generic softkey labels.
        "Да",
        "Да",
        "Нет",
        "Нет",
        "OK",
        "OK",

        // Placeholder name shown when fetching the name of an item (e.g. message author or DM) failed.
        "(неизвестно)",

        // Placeholder shown when the recipient message of a reply does not have any text content.
        "(нет содержимого)",

        // Placeholder message content shown when a message does not have any content that is supported by Discord J2ME.
        "(неподдерживаемое сообщение)",

        // Message content shown for a message that has been deleted.
        "(сообщение удалено)",

        // Title text for direct message search screen.
        "Поиск личных сообщений",

        // Guide text for text field in direct message search and "insert mention" screens.
        "Введите имя пользователя",

        // Error message shown when the searched user was not found in the direct messages list. Discord J2ME cannot initiate DM conversations based on only a username, so this message asks the user to use another client.
        "Пользователь не найден. Попробуйте начать диалог с пользователем с другого клиента.", 

        // Title text for direct message list.
        "Личные сообщения",

        // Generic "Search" softkey label. Currently used for searching for usernames in direct messages and when inserting a mention/ping.
        "Поиск",
        "Поиск",

        // Title text shown for all error message screens.
        "Ошибка",

        // Title text shown for guild (server) selector.
        "Серверы",

        // Title text shown for favorite servers list.
        "Избранные",

        // Generic "Remove" softkey command. Currently used for removing a server from the favorites list.
        "Удалить",
        "Удалить",

        // Softkey command for adding a server to the favorites list.
        "Избранное",
        "Добавить в избранное",

        // Text shown when the gateway disconnects due to an error with the heartbeat thread. As this error message is quite technical, you may simplify/generalize it to, for example, "connection error".
        "Ошибка в Heartbeat-треде",

        // Error message shown when the supplied authentication token is invalid (HTTP Unauthorized).
        "Проверьте Ваш токен",

        // Prefix of error message shown when the HTTP response has an error code.
        // The full message consists of this string and the code itself,
        // e.g. HTTP error 500
        "Ошибка HTTP ",

        // Error message shown when trying to load attachments and the CDN URL hasn't been set (is empty).
        "Адрес CDN не определен. Вложения недоступны.",

        // Parentheses. Don't change these unless your language uses a different writing system where a different type of parentheses is normally used.
        " (",
        ")",

        // Softkey labels for showing a text attachment's contents within the app.
        "Показать",
        "Показать текстом",

        // Softkey labels for showing an attachment in the device's built-in web browser.
        "Открыть",
        "Открыть в браузере",

        // Generic "Loading..." text shown in loading screen and in "Insert mention" screen.
        "Загрузка...",

        // Loading screen text shown when an attachment is being sent.
        "Отправка...",

        // Error message prefix shown when an error occurs while uploading an attachment.
        "Ошибка при отправке файла: ",

        // Generic "Skip" softkey label. Currently used for skipping an action in the key mapper.
        "Пропустить",
        "Пропустить",

        // Key press prompt shown in hotkey mapper.
        "Нажмите клавишу, чтобы исп. ее для:",

        // Names of hotkey actions shown in the key mapper.
        // These are shown after the "Press the key to use for:" string.
        // "going back" only applies to the chat view, 
        "отправки сообщения",
        "ответа на сообщение",
        "копирование содержимого сообщения",
        "обновления сообщений",
        "возврата назад",

        // Error message prefix shown when a key has been mapped to an action and the user tried to map the same key to another action. The name of the already mapped action (see above) is written after this prefix.
        "Эта клавиша уже используется для ",

        // Title text shown in login screen.
        "Вход",

        // Proxy server warning message shown at the top of the login screen.
        "Используйте только прокси, которым доверяете!",

        // Help message for finding your token. Shown in login screen above the token field.
        "Ваш токен находится в настройках разработчика в браузере (см. онлайн для уточнения). В целях безопасности рекомендуется использовать альтернативный аккаунт.",

        // "Use Wi-Fi" option shown in login screen on BlackBerry devices.
        "Использовать Wi-Fi",

        // Labels of text fields shown in the login screen.
        // You don't need to use these acronyms if they don't make sense in your language. Translations like "Server URL" and "Image server URL" are acceptable too.
        "Адрес API",
        "Адрес CDN",
        "Адрес шлюза",
        "Токен",

        // Softkey label for confirming the login options in the login screen.
        "Вход",
        "Вход",

        // Softkey command for exiting the application.
        "Выход",
        "Выход",

        // "Use gateway" option shown in login screen.
        "Использовать шлюз",

        // Label for radio button field for token sending options.
        "Отправить токен как",

        // Token sending options.
        "Заголовок (по умолчанию)",
        "JSON",
        "Параметр запроса",

        // Error messages shown when trying to login and the token or API URL fields are empty.
        "Пожалуйста, введите ваш токен",
        "Пожалуйста, укажите адрес API",

        // Main menu items.
        // "Log out" brings you back to the login screen where you enter your token and other login settings.
        "Серверы",
        "Избранные",
        "Личные сообщения",
        "Настройки",
        "Выход из аккаунта",

        // Title text shown in "Insert mention" screen (for adding a ping when writing a message).
        "Вставить упоминания",

        // Label shown for username search results in "Insert mention" screen. Shown if more than one user matches the username query.
        "Результаты поиска",

        // Message shown when the username query resulted in no matches in the "Insert mention" screen.
        "Ничего не найдено",

        // Error message shown when selecting "Insert" in the "Insert mention" screen and none of the search results (radio buttons) were picked.
        "Пользователь не выбран",

        // Prefix and suffix of status message shown when a user has been added to a group DM. The whole message is in the form "added X to the group"
        "добавил ",
        " в группу",

        // Status message shown when a user has left a group DM.
        "покинул группу",

        // Prefix and suffix of status message shown when a user has removed another user from a group DM. The whole message is in the form "removed X from the group".
        "исключил ",
        " из группы",

        // Status messages.
        "начал звонок",
        "изменил имя группы",
        "изменил значок группы",
        "закрепил сообщение",
        "вступил в сервер",
        "бустанул сервер",

        // Prefix of status message shown when a user has boosted the server and the server has reached a certain boosting level. The level is appended to the end of this string, in the form "boosted the server to level X".
        "бустанул сервер до уровня ",

        // Prefix of message content when the message is a sticker.
        // The whole message is in the form of "(sticker: Name)"
        "(стикер: ",

        // Placeholder name for a sticker when the sticker's name could not be fetched.
        "неизвестно",

        // Message timestamp hour-minute separator, day-month separator, and AM/PM indicators.
        // Note: order of day and month cannot be changed currently
        ":",
        "/",
        "A",
        "P",

        // Softkey labels for inserting a mention/ping in the "send message" screen.
        "Упоминание",
        "Вставить упоминание",

        // Prefixes of title text for "send message" screen.
        // The full title is in the form of "Send message (@user)" or "Send message (#channel)".
        "Отправить сообщение (@",
        "Отправить сообщение (#",

        // Error message shown when trying to insert a mention into a message and the gateway connection is not active.
        "Требует активное шлюзовое подключение",

        // Title text for "copy message content" screen.
        "Копировать сообщение",

        // Title text for "edit message content" screen.
        "Редактировать сообщение",

        // Title text for gateway disconnect prompt screen.
        "Отключен",

        // Main body text for gateway disconnect prompt screen.
        "Ошибка шлюза. Переподключиться?",

        // Top label for disconnection message shown in gateway disconnect prompt screen. The disconnection message is either a message sent by the Discord gateway (such as "requesting client reconnect") or a Java exception.
        "Сообщение",

        // Prefix for top body text shown in the reply form. The whole top text is in the form "Replying to @user". The contents of the recipient message are shown below this.
        "Ответ ",

        // Top label for the message entry box in the reply form.
        "Ваше сообщение:",

        // Checkbox for selecting whether to mention/ping the recipient. Shown in the reply form.
        "Упомянуть пользователя",

        // Title text for settings menu.
        "Настройки",

        // Settings screen heading for themes section.
        "Тема",

        // Theme options.
        "Темная",
        "Светлая",
        "Черная",

        // Settings screen heading for miscellaneous user interface related settings.
        "Пользовательский интерфейс",

        // Settings option for enabling the old channel view user interface (from version 1.1 and below).
        "Использовать старый польз. интерфейс",

        // Settings option for using 12-hour time format in timestamps.
        "12-часовое время",

        // Settings option for using the Java-based file picker for sending attachments. If disabled, the web-based file picker is used.
        "Стандартное средство выбора файлов",

        // Settings option for automatically reconnecting to the gateway if the connection closes.
        "Автоподключение к шлюзу",

        // Settings option for enabling icons in server and direct message lists.
        "Значки сервера/личн. сообщений",

        // Settings option for enabling nickname role colors.
        "Цвета имен",

        // Settings screen heading for message author font size.
        "Шрифт отправителя сообщения",

        // Font size options.
        "Маленький",
        "Средний",
        "Большой",

        // Settings screen heading for message content font size.
        "Шрифт содержимого сообщения",

        // Settings screen heading for message load count. This is the amount of messages that are loaded and shown at a time.
        "Макс. количество отображаемых сообщений",

        // Settings screen heading for selecting attachment file format.
        "Формат вложения",

        // Settings screen heading for maximum attachment size in pixels.
        "Макс. размер вложения",

        // Settings screen heading for profile picture shape.
        // Note: The word "avatar" was used here because "profile picture shape" is too long to fit on one row on some phones, and I didn't want to shorten it as "PFP".
        "Форма аватара",

        // Settings options for profile picture shape. "Circle (HQ)" is circle but with anti-aliasing and smoothing enabled.
        "Квадратный",
        "Круглый",
        "Круглый (выс. качество)",

        // Settings section for profile picture resolution.
        "Разрешение аватара",

        // Settings options for profile picture resolution. Placeholder means the PFPs won't get downloaded, but instead a placeholder (username's initials) is shown.
        "Только заполнитель",

        // Resolution options. Used for profile picture resolution and menu icon resolution.
        "Откл",
        "16 пикс",
        "32 пикс",

        // Settings section for menu icon size.
        "Размер значка меню",

        // Settings section for controlling the display of reply messages.
        "Показывать ответы как",

        // Settings option to show replies as only the recipient (in the form "Author -> Recipient").
        "Только получатель",

        // Settings option to show replies with the whole recipient message.
        "Полное сообщение",

        // Settings section for hotkey action management.
        "Горячие клавиши",

        // Settings option to use "default" hotkeys. When enabled, the J2ME game actions (ABCD) are used for hotkey actions, instead of user-defined key bindings. I named it "default" due to the lack of a better term for people who aren't familiar with J2ME development.
        "Горячие клавиши по умолчанию",

        // Softkey label for opening the key remapper in the settings menu. The long variant of the label is shown as the button text.
        "Настроить клавиши",
        "Изменить клавиши",

        // Generic softkey labels. Currently used in settings menu.
        "Сохранить",
        "Сохранить",
        "Отменить",
        "Отменить",

        // Error message shown when trying to open an URL (e.g. attachment)from the app, but the phone does not support opening URLs while keeping the app running in the background.
        "Приложение должно быть закрыто при открытии ссылки.",

        // Prefix of error message shown when an error occurs when trying to open an URL.
        "Ссылка не может быть открыта (",

        // Suffix of error message shown when an error occurs when trying to open an URL. The actual URL is shown after this message.
        ")\n\nВы можете попробовать скопировать ссылку вручную в браузер Вашего устройства: ",

        // Title text for URL list screen ('Open URL' option in channel view).
        "Выбрать ссылку",

        // Softkey labels for inserting a mention in the "Insert mention" screen.
        "Вставить",
        "Вставить"
    };
}