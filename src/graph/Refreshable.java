package graph;

import java.awt.*;

public interface Refreshable {
	/* called to indicate that we're starting an update */
    void start();

	/* add a node */
    Object addNode(String id, String services, String label, String description, Image image, String tooltip);

	/* setup all of our routes in one fell swoop */
    void setRoutes(Route[] routes);

	/* highlight a pivot line please */
    void highlightRoute(String src, String dst);

	/* clear any untouched nodes */
    void deleteNodes();

	/* called to indicate that we're ending an update */
    void end();
}
