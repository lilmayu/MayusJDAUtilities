<p align="center">
  <h1 align="center">Mayu's JDA Utilities</h1>
</p>
<p align="center">
  <img src="http://ForTheBadge.com/images/badges/made-with-java.svg" alt="Made with Java">
  <br>
  <img src="https://www.code-inspector.com/project/29506/status/svg" alt="Code Grade">
  <img src="https://img.shields.io/github/license/lilmayu/MayusJDAUtilities.svg" alt="License">
  <img src="https://img.shields.io/github/v/release/lilmayu/MayusJDAUtilities.svg" alt="Version">
</p>
<p align="center">
    JDA Utilities which can help with the development of Discord bots
  <br>
  Made by <a href="https://mayuna.dev">Mayuna</a>
</p>

## Installation
### Maven
```xml
<dependency>
    <groupId>dev.mayuna</groupId>
    <artifactId>mayus-jda-utilities</artifactId>
    <version>VERSION</version>
</dependency>
```
### Gradle
```gradle
repositories {
    mavenCentral()
}

dependencies {
    // Change 'implementation' to 'compile' in old Gradle versions
    implementation 'dev.mayuna:mayus-jda-utilities:VERSION'
}
```
- For version number see latest [Maven Repository](https://mvnrepository.com/artifact/dev.mayuna/mayus-jda-utilities) release (should be same with Github Release though)
- You can also use [GitHub Releases](https://github.com/lilmayu/MayusJDAUtilities/releases) or [GitHub Packages](https://github.com/lilmayu/MayusJDAUtilities/packages/)

## Documentation
### Interactive Message
You need to register MayuCoreListener class within JDA event listeners
```java
var jdaBuilder = JDABuilder.createDefault("token").addEventListeners(new MayuCoreListener());
jdaBuilder.build().awaitReady();
```

Interactive Message supports all interactable elements such as Reactions, Buttons and Select Options
```java
InteractiveMessage iMessage = InteractiveMessage.create(new MessageBuilder("Demo"));
iMessage.addInteraction(Interaction.asButton(DiscordUtils.generateButton(ButtonStyle.PRIMARY, "Next Page")), () -> {
    Message message = iMessage.getMessage();
    message.editMessage("Next page!").queue();
});
iMessage.send(messageChannel); // You can even send it to InteractionHook
```
`Interaction#asButton()` takes JDA's Button object. `DiscordUtils#generateButton()` simplifies creation of button (you don't have to worry about button id, since it is all automatically resolved). You **cannot** combine Buttons with Select Options since Discord forbids it.

Possible real world example:
```java
// We are using Select Options, so we need to use #createSelectionMenu()
InteractiveMessage iMessage = InteractiveMessage.createSelectionMenu(
    new MessageBuilder().setEmbeds(MessageInfo.informationEmbed("Please, select your birthday month.").build())
);

// Creates 12 select options with labels "1: January" etc.
for (int monthIndex = 0; monthIndex < 12; monthIndex++) {
    String month = new DateFormatSymbols().getMonths()[monthIndex]; // Gets name of the month by it's number
    iMessage.addInteraction(Interaction.asSelectOption(DiscordUtils.generateSelectOption((monthIndex + 1) + ": " + month)), () -> {
        person.setBirthdayMonth(monthIndex);

        InteractiveMessage selectedMessage = InteractiveMessage.create(
            new MessageBuilder()
                .setEmbeds(MessageInfo.successEmbed("Successfully selected " + month + " as your birthday month!").build())
        );
        // Deletes message upon interacting with Close button
        selectedMessage.addInteraction(Interaction.asButton(DiscordUtils.generateCloseButton(ButtonStyle.DANGER)), selectedMessage::delete);
        // It will aslso remove all previous components (in this case - remove select options)
        selectedMessage.edit(iMessage.getMessage());
    });
}
iMessage.send(messageChannel);

// All this without ever touching interaction listeners.
```

### Managed Message / Managed Message Channel
These can be used if you want to quicky save/load Messages or Message Channels within JSON files.
```java
// # Creating and saving Managed Message
ManagedMessageChannel managed = new ManagedMessageChannel("counting_channel", textChannel.getGuild(), textChannel);
JsonObject jsonObject = managed.toJsonObject(); // Dumps information like text channel ID and guild ID into JsonObject
// (Using Mayu's Json Utilities) Saves jsonObject to file "data.json"
JsonUtil.saveJson(jsonObject, new File("data.json"));

// # loading Managed Message Channel
// (Using Mayu's Json Utilities) Loads JsonObject from file "data.json"
JsonObject jsonObject = JsonUtil.createOrLoadJsonFromFile(new File("data.json"));
ManagedMessageChannel managed = new ManagedMessageChannel(jsonObject); // Loads IDs from jsonObject
managed.updateEntries(jda); // IMPORTANT - After loading from JSON, there are only IDs! You need to get Guild and MessageChannel information from Discord using JDA to use them (well, using #updateEntires() method).
MessageChannel messageChannel = managed.getMessageChannel();
```
**This applies to `ManagedMessage` aswell.**

### MessageInfo
Documentation coming soon:tm:

### Miscelenious - Commands, DiscordUtils
Documentation coming soon:tm:
