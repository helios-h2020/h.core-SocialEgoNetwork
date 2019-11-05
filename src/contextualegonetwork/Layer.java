package contextualegonetwork;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.function.BiFunction;

/**
 * This class implements a layer of the Contextual Ego Network, that is a conceptual structure made of a context
 * and its translation function. The latter maps node between contexts. This library provides a default translation function
 * that, given a node belonging to a context, looks for the corresponding node in another context using the node's numerical
 * identifier to do the mapping. This function can be overwritten at construction time of the layer.
 */
class Layer {
    /**
     * The context encapsulated in this layer
     */
    private Context context;
    /**
     * The translation function for the context encapsulated in this layer
     */
    private BiFunction<Context,Integer,Node> translationFunction;

    /**
     * Used in deserialization
     */
    @JsonCreator
    public Layer()
    {}
    /**
     * Constructor method
     * @param c The context to be wrapped in this layer
     * @param f The translation function (if null, the default one will be assigned to the layer)
     * @throws NullPointerException if c is null
     */
    public Layer(Context c, BiFunction<Context,Integer,Node> f) {
        if(c == null) throw new NullPointerException();
        this.context = c;
        if(f == null)  {
            BiFunction<Context, Integer, Node> fun = (ctx, i) -> ctx.findNodeById(i);
            this.translationFunction = fun;
        }
        else this.translationFunction = f;
    }

    /**
     * @return The context encapsulated in this layer
     */
    public Context getContext() {
        return this.context;
    }

    /**
     * @return The translation function associated to the context encapsulated in this layer
     */
    public BiFunction<Context,Integer,Node> getTranslationFunction() {
        return this.translationFunction;
    }

    /**
     * Sets a new translation function for this context
     * @param f The translation function to be set
     * @throws NullPointerException if f is null
     */
    public void setTranslationFunction(BiFunction <Context,Integer,Node> f) {
        if(f == null) throw new NullPointerException();
        this.translationFunction = f;
    }
}
