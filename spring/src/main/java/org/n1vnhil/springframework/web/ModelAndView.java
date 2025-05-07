package org.n1vnhil.springframework.web;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {

    private String view;

    private Map<String, String> context = new HashMap<>();

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public Map<String, String> getContext() {
        return context;
    }
}
