package com.konkerlabs.platform.registry.business.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static List<Location> listNewLocationns(Location currentTree, Location newTree) {

        List<Location> currentNodes = getNodesList(currentTree);
        List<Location> newNodes = getNodesList(newTree);

        Map<String, Location> currentNodesMap = getNodesMapByName(newNodes);

        for (Location location : currentNodes) {
            currentNodesMap.remove(location.getName());
        }

        return new ArrayList<Location>(currentNodesMap.values());

    }

    public static List<Location> listRemovedLocationns(Location currentTree, Location newTree) {

        List<Location> currentNodes = getNodesList(currentTree);
        List<Location> newNodes = getNodesList(newTree);

        Map<String, Location> currentNodesMap = getNodesMapByName(currentNodes);

        for (Location location : newNodes) {
            currentNodesMap.remove(location.getName());
        }

        return new ArrayList<Location>(currentNodesMap.values());

    }

    public static List<Location> listExistingLocationns(Location currentTree, Location newTree) {

        List<Location> existingLocations = new ArrayList<>();

        List<Location> currentNodes = getNodesList(currentTree);
        List<Location> newNodes = getNodesList(newTree);

        Map<String, Location> currentNodesMap = getNodesMapByName(currentNodes);

        for (Location location : newNodes) {
            if (currentNodesMap.containsKey(location.getName())) {
                existingLocations.add(location);
            }
        }

        return existingLocations;
    }

    private static Map<String, Location> getNodesMapByName(List<Location> currentNodes) {

        Map<String, Location> locationsMap = new HashMap<>();

        for (Location location : currentNodes) {
            locationsMap.put(location.getName(), location);
        }

        return locationsMap;

    }

}
