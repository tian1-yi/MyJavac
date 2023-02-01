package com.myj.tools.javac.comp;

import com.myj.tools.javac.util.Context;

import java.util.ArrayList;
import java.util.List;

public class Todo {

    public final static Context.Key<Todo> todoKey = new Context.Key<>();

    public List<Env<AttrContext>> todo = new ArrayList<>();

    public static Todo instance(Context context) {
        Todo to = context.get(todoKey);
        if (to == null) {
            to = new Todo(context);
        }
        return to;
    }

    private Todo(Context context) {
        context.put(todoKey, this);
    }

    public void add(Env<AttrContext> env) {
        todo.add(env);
    }

    public Env<AttrContext> get(int i) {
        return todo.get(i);
    }
}
