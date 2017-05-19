package com.konkerlabs.platform.registry.business.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.konkerlabs.platform.registry.business.model.Location;

public class LocationTreeUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(LocationTreeUtils.class);

    public static Location buildTree(List<Location> allNodes) {

        Location root = null;

        Map<String, List<Location>> childrenListMap = new HashMap<>();

        for (Location location: allNodes) {
            if (location.getParent() == null) {
                root = location;
            } else {
                String parentGuid = location.getParent().getGuid();
                List<Location> childrens = childrenListMap.get(parentGuid);
                if (childrens == null) {
                    childrens = new ArrayList<>();
                    childrenListMap.put(parentGuid, childrens);
                }
                childrens.add(location);
             }
        }

        for (Location location: allNodes) {
            List<Location> childrens = childrenListMap.get(location.getGuid());
            if (childrens == null) {
                childrens = new ArrayList<>();
            }
            location.setChildrens(childrens);
        }

        return root;
    }

    public static List<Location> getNodesList(Location root) {
        if (root == null) {
            return null;
        }

        List<Location> nodes = new ArrayList<>();
        nodes.add(root);

        if (root.getChildrens() != null) {
            for (Location child: root.getChildrens()) {
                List<Location> childNodes = getNodesList(child);
                if (childNodes != null && !childNodes.isEmpty()) {
                    nodes.addAll(childNodes);
                }
            }
        }

        return nodes;
    }

    public static List<Location> getNodesListBreadthFirstOrder(Location root) {
        if (root == null) {
            return null;
        }

        List<Location> nodes = new ArrayList<>();

        Queue<Location> queue = new LinkedList<Location>() ;
        queue.add(root);

        while(!queue.isEmpty()) {
            Location node = queue.remove();
            nodes.add(node);

            if (node.getChildrens() != null) {
                queue.addAll(node.getChildrens());
            }
        }

        return nodes;
    }

    public static Location searchLocationByName(Location root, String locationName, int deep) {

        if (deep > 50) {
            LOGGER.warn("Too deep structure. Cyclic graph?");
            return null;
        }

        if (root == null || root.getName().equals(locationName)) {
            return root;
        }

        for (Location child : root.getChildrens()) {
            Location element = searchLocationByName(child, locationName, deep + 1);
            if (element != null) {
                return element;
            }
        }

        return null;

    }

    public static List<Location> listNewLocations(Location currentTree, Location newTree) {

        List<Location> newNodes = new ArrayList<>();

        List<Location> newTreeNodes = getNodesListBreadthFirstOrder(newTree);
        Set<String> currentNodesSet = getNodesSetName(getNodesList(currentTree));

        for (Location location : newTreeNodes) {
            if (!currentNodesSet.contains(location.getName())) {
                newNodes.add(location);
            }
        }

        // deepest location first
        Collections.reverse(newNodes);

        return newNodes;

    }

    public static List<Location> listRemovedLocations(Location currentTree, Location newTree) {

        List<Location> removedNodes = new ArrayList<>();

        List<Location> currrentNodes = getNodesListBreadthFirstOrder(currentTree);
        Set<String> newNodesSet = getNodesSetName(getNodesList(newTree));

        for (Location location : currrentNodes) {
            if (!newNodesSet.contains(location.getName())) {
                removedNodes.add(location);
            }
        }

        // deepest location first
        Collections.reverse(removedNodes);

        return removedNodes;

    }

    public static List<Location> listExistingLocations(Location currentTree, Location newTree) {

        List<Location> existingLocations = new ArrayList<>();

        Set<String> currentNodesMap = getNodesSetName(getNodesList(currentTree));
        List<Location> newNodes = getNodesList(newTree);

        for (Location location : newNodes) {
            if (currentNodesMap.contains(location.getName())) {
                existingLocations.add(location);
            }
        }

        return existingLocations;
    }

    private static Set<String> getNodesSetName(List<Location> currentNodes) {

        Set<String> locationsSet = new HashSet<>();

        for (Location location : currentNodes) {
            locationsSet.add(location.getName());
        }

        return locationsSet;

    }

}
