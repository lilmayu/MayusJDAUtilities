# Mayu's JDA Utilities

## Information
- Current version: `2.0_pre0`
- This library is **not** a fork of [JDA-Utilities](https://github.com/JDA-Applications/JDA-Utilities)
- Libraries used:
  - [JDA](https://github.com/DV8FromTheWorld/JDA) (4.3.0_331)
  - [JDA-Chewtils](https://github.com/Chew/JDA-Chewtils) (1.22.0)
  - [Mayu's Library](https://github.com/lilmayu/MayusLibrary) (0.1)
  - [Mayu's Json Utilities](https://github.com/lilmayu/MayusJsonUtilities) (1.2)
  - GSON (2.8.8)
  - Lombok (1.18.20)

## Installation
With gradle (below 7.0, after 7.0 replace "compile" with "implementation"):
```groovy
repositories {
    mavenCentral() // For transitive dependencies
    maven { // For JDA
      name 'm2-dv8tion'
      url 'https://m2.dv8tion.net/releases'
    }
    maven { url "https://m2.chew.pro/releases" } // For JDA-Chewtils
    flatDir { dirs 'libs' } // For local libraries (in root of project) (TEMPORARY SOLUTION)
}

dependencies {
    compile 'net.dv8tion:JDA:4.3.0_331'
    compile 'pw.chew:jda-chewtils:1.22.0'
    compile 'com.google.code.gson:gson:2.8.8'
    compile name: 'MayusJsonUtils-1.2'
    compile name: 'MayusLibrary-0.1'
}
```

## Documentation

### Interactive Messages
- Note: These might change in future updates
- You must register MayuCoreListener class for those to work. Example:
```java
JDA jda = JDABuilder.createDefault(/* bot token */)
  .addEventListeners(new MayuCoreListener())
  .build();
```
- Example usage of Interactive Messages
```java
// Creates message with "Done" and "Close" buttons
InteractiveMessage interactiveMessage = new InteractiveMessage(new MessageBuilder("Please, click Done or Close."));
interactiveMessage.addInteraction(MessageInteraction.asButton(Button.primary("done_button", "Done")), () -> { // Button ids can be random
    messageChannel.sendMessage("You have clicked Done button!");
    interactiveMessage.delete(); // Removes Interaction message from message channel and internal registers of this library
});
interactiveMessage.addInteraction(MessageInteraction.asButton(Button.danger("close_button", "Close")), () -> {
    interactiveMessage.delete();
});
interactiveMessage.sendMessage(messageChannel);

// Creates message with multiple Select options
InteractiveMessage interactiveMessage = new InteractiveMessage(new MessageBuilder("Please, click Done or Close."));
interactiveMessage.setSelectionMenuBuilder(SelectionMenu.create("some_menu")); // Menu ID can be random
interactiveMessage.setDeleteMessageAfterInteraction(true); // Will delete Interaction Message upon user interaction
interactiveMessage.addInteraction(MessageInteraction.asSelectOption(SelectOption.of("Do this!", "do_this_option")), () -> {
    messageChannel.sendMessage("Clicked Do this!!");
});
interactiveMessage.addInteraction(MessageInteraction.asSelectOption(SelectOption.of("Do that!", "do_that_option")), () -> {
    messageChannel.sendMessage("Clicked Do that!!");
});
interactiveMessage.sendMessage(messageChannel);
```
- Some methods in `InteractiveMessage`

| Name | Arguments | Description |
|------|-----------|-------------|
| `#sendMessage()` | `MessageChannel` | Sents Interactive message in specified Message Channel |
| `#addInteraction()` | `MessageInteraction`, `Runnable` | Adds interaction to Interactive Message |
| `#setWhitelistUsers()` | `boolean` | Sets flag in Interactive message to ignore all users that are not Whitelisted |
| `#addWhitelistUser()` | `User` | Adds user to Whitelist |
| `#setDeleteMessageAfterInteraction()` | `boolean` | Sets flag in Interactive message to delete itself upon user interaction |

- Limitations
  - Maximum number of Buttons and Select Option is **25**
  - Maximum number of Reactions is **20**
  - You cannot mix Buttons and Select Options together
  - For now, you can't make Interaction Messages ephemeral

### Managed
 - Managed classes like: `ManagedUser`, `ManagedGuild`, `ManagedMessage`, `ManagedMessageChannel` are used to make easier development with users/guilds/messages/message channels which may developer want to be saved locally - for later use (for example: ManagedMessageChannel used for keeping which channel was made for Counting, etc..)
 - Documentation coming soon.
