package pl.ismop.web.client.widgets.analysis.dummy;

public interface IDummyView {
    void setTitle(String title);

    interface IDumyPresenter {
        void showDevice1();
        void showDevice2();
        void selectDevice1();
        void unselectDevice1();
        void selectDevice2();
        void unselectDevice2();
        void showSection1();
        void showSection2();
        void showProfile1();
        void showProfile2();
        void clearMinimap();
    }
}
