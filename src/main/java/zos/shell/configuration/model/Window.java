package zos.shell.configuration.model;

public class Window {

    private String fontsize;
    private String fontBold;
    private String textColor;
    private String backGroundColor;

    public String getFontsize() {
        return fontsize;
    }

    public void setFontsize(final String fontsize) {
        this.fontsize = fontsize;
    }

    public String getFontbold() {
        return fontBold;
    }

    public void setFontbold(final String fontbold) {
        this.fontBold = fontbold;
    }

    public String getTextcolor() {
        return textColor;
    }

    public void setTextcolor(final String textColor) {
        this.textColor = textColor;
    }

    public String getBackgroundcolor() {
        return backGroundColor;
    }

    public void setBackgroundcolor(final String backGroundColor) {
        this.backGroundColor = backGroundColor;
    }

    @Override
    public String toString() {
        return "Window{" +
                "fontsize='" + fontsize + '\'' +
                ", fontBold='" + fontBold + '\'' +
                ", textColor='" + textColor + '\'' +
                ", backGroundColor='" + backGroundColor + '\'' +
                '}';
    }

}