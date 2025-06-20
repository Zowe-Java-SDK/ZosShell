package zos.shell.singleton.configuration.model;

@SuppressWarnings("unused")
public class Window {

    private String fontsize;
    private String fontBold;
    private String textColor;
    private String backGroundColor;
    private String paneHeight;
    private String paneWidth;

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

    public String getPaneHeight() {
        return paneHeight;
    }

    public void setPaneHeight(String paneHeight) {
        this.paneHeight = paneHeight;
    }

    public String getPaneWidth() {
        return paneWidth;
    }

    public void setPaneWidth(String paneWidth) {
        this.paneWidth = paneWidth;
    }

    @Override
    public String toString() {
        return "Window{" +
                "fontsize='" + fontsize + '\'' +
                ", fontBold='" + fontBold + '\'' +
                ", textColor='" + textColor + '\'' +
                ", backGroundColor='" + backGroundColor + '\'' +
                ", paneHeight='" + paneHeight + '\'' +
                ", paneWidth='" + paneWidth + '\'' +
                '}';
    }

}
