package pl.ismop.web.client.widgets.common.panel;

import com.google.gwt.user.client.ui.IsWidget;

public interface IPanelView extends IsWidget {
    interface IPanelPresenter {
        void close();
        void moveUp();
        void moveDown();
    }

    void setTitle(String title);
    void setWidget(IsWidget widget);
    void setFirst(boolean first);
    void setLast(boolean last);
}
