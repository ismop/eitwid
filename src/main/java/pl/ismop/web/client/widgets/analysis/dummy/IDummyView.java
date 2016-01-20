package pl.ismop.web.client.widgets.analysis.dummy;

public interface IDummyView {
    void setTitle(String title);

    interface IDumyPresenter {
        void addDevice();
        void rmDevice();
        void addSection();
        void rmSection();
        void addProfile();
        void rmProfile();

        void selectDevice();
        void unselectDevice();
        void selectSection();
        void unselectSection();
        void selectProfile();
        void unselectProfile();

        void highlightDevice();
        void unhighlightDevice();
        void highlightSection();
        void unhighlightSection();
        void highlightProfile();
        void unhighlightProfile();
    }
}
