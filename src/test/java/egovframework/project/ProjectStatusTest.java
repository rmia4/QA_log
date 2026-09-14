package egovframework.project;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import egovframework.project.gubun.ProjectStatus;

public class ProjectStatusTest {

    @Test
    public void statusesAreOrderedForProjectListGroups() {
        List<String> codes = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (ProjectStatus status : ProjectStatus.values()) {
            codes.add(status.getCode());
            labels.add(status.getLabel());
        }

        assertEquals(asList("in_progress", "maintenance", "on_hold", "archived"), codes);
        assertEquals(asList("진행중", "유지보수중", "보류", "보관"), labels);
    }
}
