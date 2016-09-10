package lib.carl93.example;

/**
 * Created by Carl on 2016-09-10 010.
 */
public class ItemEntity {
    private String title;
    private String desc;

    public ItemEntity(String title, String desc) {
        this.title = title;
        this.desc = desc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
