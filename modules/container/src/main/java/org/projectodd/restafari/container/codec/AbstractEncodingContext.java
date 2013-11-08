package org.projectodd.restafari.container.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.projectodd.restafari.container.aspects.ResourceAspectManager;
import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class AbstractEncodingContext<T> implements EncodingContext<T> {

    public AbstractEncodingContext(AbstractEncodingContext<T> parent, RequestContext ctx, Object object, Runnable completionHandler) {
        this.parent = parent;
        this.ctx = ctx;
        this.object = object;
        this.completionHandler = completionHandler;

    }

    public RequestContext requestContext() {
        return ctx;
    }

    public int depth() {
        if (this.parent == null) {
            return 0;
        }
        return this.parent.depth() + 1;
    }

    public T attachment() {
        if (this.parent != null) {
            return this.parent.attachment();
        }
        return null;
    }

    public ResourceAspectManager aspectManager() {
        if (this.parent != null) {
            return this.parent.aspectManager();
        }
        return null;
    }

    public AbstractEncodingContext parent() {
        return this.parent;
    }

    public ResourceEncoder encoder() {
        if (this.parent != null) {
            return this.parent.encoder();
        }
        return null;
    }

    public void encode() throws Exception {
        if (this.object instanceof Resource) {
            aspectManager().stream().forEach((aspect) -> {
                Resource resource = aspect.forResource((Resource) this.object);
                if (resource != null) {
                    this.aspectResources.add(resource);
                }
            });
        }

        encoder().encode(this);
    }

    public Object object() {
        return this.object;
    }

    public void end() {
        this.completionHandler.run();
    }

    public boolean shouldEncodeContent() {
        if (encoder() instanceof ExpansionControllingEncoder) {
            return ((ExpansionControllingEncoder) encoder()).shouldEncodeContent(this);
        }


        if (this.object instanceof PropertyResource) {
            return true;
        }

        if (this.parent != null && this.parent.object instanceof CollectionResource) {
            return depth() < 2;
        }

        return depth() < 1;
    }

    public void encodeContent(Runnable endContentHandler) {
        if (shouldEncodeContent()) {
            this.endContentHandler = endContentHandler;

            if (this.object instanceof CollectionResource) {
                ((CollectionResource) this.object).readMembers(ctx, new MyCollectionContentSink());
            } else if (this.object instanceof ObjectResource) {
                ((ObjectResource) this.object).readMembers(ctx, new MyObjectContentSink());
            } else if (this.object instanceof PropertyResource) {
                ((PropertyResource) this.object).readContent(ctx, new MyPropertyContentSink());
            } else if (this.object instanceof BinaryResource) {
                ((BinaryResource) this.object).readContent(ctx, new MyBinaryContentSink());
            }
        } else {
            endContentHandler.run();
        }
    }

    public boolean hasAspects() {
        return !this.aspectResources.isEmpty();
    }

    @Override
    public void encodeAspects(Runnable endContentHandler) {
        this.endContentHandler = endContentHandler;
        if (this.object instanceof Resource) {
            MyObjectContentSink sink = new MyObjectContentSink();
            this.aspectResources.stream().forEach((aspectResource) -> {
                Resource r =  new SimplePropertyResource((Resource) this.object, aspectResource.id(), aspectResource);
                sink.accept( r );
            });
            sink.close();
        }
    }

    protected void encodeNextContent() {
        if (children.isEmpty()) {
            endContentHandler.run();
        } else {
            EncodingContext next = children.removeFirst();
            try {
                next.encode();
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    private AbstractEncodingContext<T> parent;
    private final RequestContext ctx;
    private Object object;
    private Runnable endContentHandler;
    private Runnable completionHandler;
    private LinkedList<EncodingContext> children = new LinkedList<>();
    private List<Resource> aspectResources = new ArrayList<>();

    private class MyCollectionContentSink implements ResourceSink {

        @Override
        public void accept(Resource resource) {
            ChildEncodingContext child = new ChildEncodingContext(ctx, AbstractEncodingContext.this, resource);
            children.add(child);
        }

        @Override
        public void close() {
            encodeNextContent();
        }

    }

    private class MyObjectContentSink implements ResourceSink {
        @Override
        public void accept(Resource resource) {
            ChildEncodingContext child = new ChildEncodingContext(ctx, AbstractEncodingContext.this, resource);
            children.add(child);
        }

        @Override
        public void close() {
            encodeNextContent();
        }
    }

    private class MyPropertyContentSink implements PropertyContentSink {

        @Override
        public void accept(Object o) {
            ChildEncodingContext child = new ChildEncodingContext(ctx, AbstractEncodingContext.this, o);
            children.add(child);
            encodeNextContent();
        }
    }

    private class MyBinaryContentSink implements BinaryContentSink {

        private ByteBuf buffer = Unpooled.buffer();

        @Override
        public void accept(ByteBuf byteBuf) {
            buffer.writeBytes(byteBuf);
        }

        @Override
        public void close() {
            ChildEncodingContext child = new ChildEncodingContext(ctx, AbstractEncodingContext.this, buffer);
            children.add(child);
            encodeNextContent();
        }

    }


}
