package com.imsweb.algorithms.seersiterecode;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: depryf
 * Date: 8/22/12
 */
public class SeerSiteGroupDto {

    /** Unique identifier (identifiers might change from one version to another) */
    private String _id;

    /** Name of the group */
    private String _name;

    /** Indentation level */
    private Integer _level;

    /** Site inclusions */
    private String _siteInclusions;

    /** Site exclusions */
    private String _siteExclusions;

    /** Histology inclusions */
    private String _histologyInclusions;

    /** Histology exclusions */
    private String _histologyExclusions;

    /** Recode */
    private String _recode;

    /** Children codes */
    private List<String> _childrenRecodes;

    public String getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }

    public Integer getLevel() {
        return _level;
    }

    public String getSiteInclusions() {
        return _siteInclusions;
    }

    public String getSiteExclusions() {
        return _siteExclusions;
    }

    public String getHistologyInclusions() {
        return _histologyInclusions;
    }

    public String getHistologyExclusions() {
        return _histologyExclusions;
    }

    public String getRecode() {
        return _recode;
    }

    public List<String> getChildrenRecodes() {
        return _childrenRecodes;
    }

    public void setId(String id) {
        _id = id;
    }

    public void setName(String name) {
        _name = name;
    }

    public void setLevel(Integer level) {
        _level = level;
    }

    public void setSiteInclusions(String siteInclusions) {
        _siteInclusions = siteInclusions;
    }

    public void setSiteExclusions(String siteExclusions) {
        _siteExclusions = siteExclusions;
    }

    public void setHistologyInclusions(String histologyInclusions) {
        _histologyInclusions = histologyInclusions;
    }

    public void setHistologyExclusions(String histologyExclusions) {
        _histologyExclusions = histologyExclusions;
    }

    public void setRecode(String recode) {
        _recode = recode;
    }

    public void setChildrenRecodes(List<String> childrenRecodes) {
        _childrenRecodes = childrenRecodes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SeerSiteGroupDto that = (SeerSiteGroupDto)o;

        return !(_id != null ? !_id.equals(that._id) : that._id != null);

    }

    @Override
    public int hashCode() {
        return _id != null ? _id.hashCode() : 0;
    }
}
