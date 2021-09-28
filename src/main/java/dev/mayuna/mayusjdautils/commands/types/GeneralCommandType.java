package dev.mayuna.mayusjdautils.commands.types;

public class GeneralCommandType extends BaseCommandType {

    @Override
    public String getName() {
        return "GENERAL";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof GeneralCommandType))
            return false;

        GeneralCommandType that = (GeneralCommandType) o;
        return getName().equals(that.getName());
    }
}
