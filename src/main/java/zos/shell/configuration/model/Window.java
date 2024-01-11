package zos.shell.configuration.model;

public class Window {

    private String fontsize;

    private String fontbold;

    private String textcolor;
    private String backgroundcolor;

    public String getFontsize() {
        return fontsize;
    }

    public void setFontsize(final String fontsize) {
        this.fontsize = fontsize;
    }

    public String getFontbold() {
        return fontbold;
    }

    public void setFontbold(final String fontbold) {
        this.fontbold = fontbold;
    }

    public String getTextcolor() {
        return textcolor;
    }

    public void setTextcolor(final String textcolor) {
        this.textcolor = textcolor;
    }

    public String getBackgroundcolor() {
        return backgroundcolor;
    }

    public void setBackgroundcolor(final String backgroundcolor) {
        this.backgroundcolor = backgroundcolor;
    }

    @Override
    public String toString() {
        return "Window{" +
                "fontsize='" + fontsize + '\'' +
                ", fontbold='" + fontbold + '\'' +
                ", textcolor='" + textcolor + '\'' +
                ", backgroundcolor='" + backgroundcolor + '\'' +
                '}';
    }

}