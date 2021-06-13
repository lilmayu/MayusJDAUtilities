package lilmayu.mayusjdautilities.objects;

import lombok.Getter;

public class Argument {

    private @Getter final String value;

    public Argument(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

}
