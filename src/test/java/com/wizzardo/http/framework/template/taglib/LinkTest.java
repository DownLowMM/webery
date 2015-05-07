package com.wizzardo.http.framework.template.taglib;

import com.wizzardo.http.framework.Controller;
import com.wizzardo.http.framework.WebApplicationTest;
import com.wizzardo.http.framework.template.Renderer;
import com.wizzardo.http.framework.template.TagLib;
import com.wizzardo.http.framework.template.taglib.g.Link;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by wizzardo on 26.04.15.
 */
public class LinkTest extends WebApplicationTest {

    public static class BookController extends Controller {

        public Renderer list() {
            return renderString("list of books");
        }

        public Renderer get() {
            return renderString("some book");
        }
    }

    @Override
    public void setUp() throws NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
        super.setUp();
        TagLib.findTags(Collections.singletonList(Link.class));
        server.getUrlMapping()
                .append("/book/list", BookController.class, "list")
                .append("/book/$id", BookController.class, "get")
        ;
    }

    @Test
    public void testLinkTag() {
        Map<String, String> attrs = new LinkedHashMap<String, String>();
        Map<String, Object> model = new LinkedHashMap<String, Object>();

        attrs.put("controller", "book");
        attrs.put("action", "list");
        Assert.assertEquals("<a href=\"/book/list\"/>", new Link(new LinkedHashMap<>(attrs), null).get(model).toString());

        attrs.put("absolute", "true");
        Assert.assertEquals("<a href=\"http://localhost:9999/book/list\"/>", new Link(new LinkedHashMap<>(attrs), null).get(model).toString());

        attrs.put("base", "http://ya.ru");
        Assert.assertEquals("<a href=\"http://ya.ru/book/list\"/>", new Link(new LinkedHashMap<>(attrs), null).get(model).toString());

        attrs.put("fragment", "some_fragment");
        Assert.assertEquals("<a href=\"http://ya.ru/book/list#some_fragment\"/>", new Link(new LinkedHashMap<>(attrs), null).get(model).toString());

        attrs.put("params", "[key:'value']");
        Assert.assertEquals("<a href=\"http://ya.ru/book/list?key=value#some_fragment\"/>", new Link(new LinkedHashMap<>(attrs), null).get(model).toString());

        attrs.put("params", "[key:'value', key2: 'value2']");
        Assert.assertEquals("<a href=\"http://ya.ru/book/list?key=value&key2=value2#some_fragment\"/>", new Link(new LinkedHashMap<>(attrs), null).get(model).toString());


        attrs.clear();
        attrs.put("controller", "book");
        attrs.put("action", "get");
        attrs.put("params", "[id:1]");
        Assert.assertEquals("<a href=\"/book/1\"/>", new Link(new LinkedHashMap<>(attrs), null).get(model).toString());

        attrs.put("class", "red");
        attrs.put("id", "link1");
        Assert.assertEquals("<a href=\"/book/1\" class=\"red\" id=\"link1\"/>", new Link(new LinkedHashMap<>(attrs), null).get(model).toString());


        attrs.clear();
        attrs.put("controller", "book");
        attrs.put("action", "get");
        attrs.put("params", "[id:id]");
        model.put("id", 1);
        Assert.assertEquals("<a href=\"/book/1\"/>", new Link(new LinkedHashMap<>(attrs), null).get(model).toString());

        attrs.put("id", "link_${id}");
        Assert.assertEquals("<a href=\"/book/1\" id=\"link_1\"/>", new Link(new LinkedHashMap<>(attrs), null).get(model).toString());

    }
}
