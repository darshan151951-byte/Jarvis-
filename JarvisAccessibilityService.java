package com.jarvis.ai;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.os.Bundle;
import java.util.List;

public class JarvisAccessibilityService extends AccessibilityService {

    public static JarvisAccessibilityService instance;

    @Override public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}

    public boolean clickText(String text) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return false;
        return clickRecursive(root, text);
    }

    private boolean clickRecursive(AccessibilityNodeInfo node, String text) {
        CharSequence t = node.getText();
        CharSequence d = node.getContentDescription();
        if ((t != null && t.toString().equalsIgnoreCase(text)) ||
            (d != null && d.toString().equalsIgnoreCase(text))) {
            if (node.isClickable()) return node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null && clickRecursive(child, text)) return true;
        }
        return false;
    }

    public boolean typeText(String text) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return false;
        AccessibilityNodeInfo field = findEditable(root);
        if (field == null) return false;
        Bundle args = new Bundle();
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        return field.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
    }

    private AccessibilityNodeInfo findEditable(AccessibilityNodeInfo node) {
        if (node.isEditable()) return node;
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                AccessibilityNodeInfo found = findEditable(child);
                if (found != null) return found;
            }
        }
        return null;
    }

    public boolean pressBack() {
        return performGlobalAction(GLOBAL_ACTION_BACK);
    }

    public boolean goHome() {
        return performGlobalAction(GLOBAL_ACTION_HOME);
    }

    public boolean openRecents() {
        return performGlobalAction(GLOBAL_ACTION_RECENTS);
    }
}
