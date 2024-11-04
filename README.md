Don't let your streams be dreams.

I wanted a simple video streaming service that could stream from browser to browser. This was surprisingly difficult since a viewer needs the webm container metadata and can only start streaming at specific byte boundaries.

The solution is to have the viewer use a predefined webm container preamble and then wait for the next video cluster before playing. Warning: the container preamble has a lot of assumptions about the video baked in.

- A Java webm (matroska) parser, for understanding video containers
- A simple websocket relay server
- A javascript streamer / viewer
