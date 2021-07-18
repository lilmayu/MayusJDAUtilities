package lilmayu.mayusjdautilities.managed;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class ManagedMessageTypeAdapter extends TypeAdapter<ManagedMessage> {

    @Override
    public void write(JsonWriter out, ManagedMessage value) throws IOException {
        out.beginObject();
        out.name("name").value(value.getName());
        out.name("guildID").value(value.getGuildID());
        out.name("messageChannelID").value(value.getMessageChannelID());
        out.name("messageID").value(value.getMessageID());
        out.endObject();
    }

    @Override
    public ManagedMessage read(JsonReader in) throws IOException {
        String name = "invalid_json";
        long guildID = 0, messageChannelID = 0, messageID = 0;

        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "name": {
                    name = in.nextString();
                    break;
                }
                case "guildID": {
                    guildID = in.nextLong();
                    break;
                }
                case "messageChannelID": {
                    messageChannelID = in.nextLong();
                    break;
                }
                case "messageID": {
                    messageID = in.nextLong();
                    break;
                }
            }
        }
        in.endObject();

        return new ManagedMessage(name, guildID, messageChannelID, messageID);
    }
}
