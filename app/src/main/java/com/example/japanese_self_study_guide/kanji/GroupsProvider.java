package com.example.japanese_self_study_guide.kanji;

import java.util.ArrayList;
import java.util.List;

public class GroupsProvider {

    public static List<ExerciseGroup> getGroups() {
        List<ExerciseGroup> groups = new ArrayList<>();

        groups.add(new ExerciseGroup("1–14", 1, 14, 64));
        groups.add(new ExerciseGroup("15–25", 15, 25, 44));
        groups.add(new ExerciseGroup("26–34", 26, 34, 36));
        groups.add(new ExerciseGroup("35–47", 35, 47, 52));
        groups.add(new ExerciseGroup("48–55", 48, 55, 55));

        groups.add(new ExerciseGroup("Общее повторение 1", 1, 55, 80));

        groups.add(new ExerciseGroup("56–60", 56, 60, 20));
        groups.add(new ExerciseGroup("61–70", 61, 70, 40));
        groups.add(new ExerciseGroup("71–77", 71, 77, 28));
        groups.add(new ExerciseGroup("78–91", 78, 91, 56));
        groups.add(new ExerciseGroup("92–98", 92, 98, 28));

        groups.add(new ExerciseGroup("Общее повторение 2", 56, 98, 80));

        groups.add(new ExerciseGroup("99–109", 99, 109, 44));
        groups.add(new ExerciseGroup("110–116", 110, 116, 28));
        groups.add(new ExerciseGroup("117–122", 117, 122, 24));
        groups.add(new ExerciseGroup("123–136", 123, 136, 56));
        groups.add(new ExerciseGroup("137–149", 137, 149, 52));

        groups.add(new ExerciseGroup("Общее повторение 3", 99, 149, 80));

        groups.add(new ExerciseGroup("150–164", 150, 164, 40));
        groups.add(new ExerciseGroup("165–172", 165, 172, 40));
        groups.add(new ExerciseGroup("173–178", 173, 178, 40));
        groups.add(new ExerciseGroup("179–188", 179, 188, 40));
        groups.add(new ExerciseGroup("189–196", 189, 196, 40));

        groups.add(new ExerciseGroup("Общее повторение 4", 150, 196, 60));

        groups.add(new ExerciseGroup("197–204", 197, 204, 40));
        groups.add(new ExerciseGroup("205–216", 205, 216, 40));
        groups.add(new ExerciseGroup("217–224", 217, 224, 40));
        groups.add(new ExerciseGroup("225–236", 225, 236, 40));
        groups.add(new ExerciseGroup("237–246", 237, 246, 40));

        groups.add(new ExerciseGroup("Общее повторение 5", 197, 246, 60));

        groups.add(new ExerciseGroup("247–256", 247, 256, 40));
        groups.add(new ExerciseGroup("257–266", 257, 266, 40));
        groups.add(new ExerciseGroup("267–274", 267, 274, 40));
        groups.add(new ExerciseGroup("275–283", 275, 283, 40));
        groups.add(new ExerciseGroup("284–294", 284, 294, 40));

        groups.add(new ExerciseGroup("Общее повторение 6", 247, 294, 60));

        groups.add(new ExerciseGroup("295–304", 295, 304, 40));
        groups.add(new ExerciseGroup("305–312", 305, 312, 40));
        groups.add(new ExerciseGroup("313–321", 313, 321, 40));
        groups.add(new ExerciseGroup("322–329", 322, 329, 40));
        groups.add(new ExerciseGroup("330–338", 330, 338, 40));

        groups.add(new ExerciseGroup("Общее повторение 7", 295, 338, 60));

        groups.add(new ExerciseGroup("339–346", 339, 346, 40));
        groups.add(new ExerciseGroup("347–358", 347, 358, 40));
        groups.add(new ExerciseGroup("359–367", 359, 367, 40));
        groups.add(new ExerciseGroup("368–376", 368, 376, 40));
        groups.add(new ExerciseGroup("377–384", 377, 384, 40));

        groups.add(new ExerciseGroup("Общее повторение 8", 339, 384, 60));

        groups.add(new ExerciseGroup("385–396", 385, 396, 40));
        groups.add(new ExerciseGroup("397–408", 397, 408, 40));
        groups.add(new ExerciseGroup("409–418", 409, 418, 40));
        groups.add(new ExerciseGroup("419–428", 419, 428, 40));

        groups.add(new ExerciseGroup("Общее повторение 9", 385, 428, 60));

        groups.add(new ExerciseGroup("Окончание курса", 1, 428, 300));

        return groups;
    }
}
