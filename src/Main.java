public class Main {

    public static void main(String[] args) {
        methodForTesting();
    }

    public static void methodForTesting() {
        TaskManager taskManager = new TaskManager();

        Task newTask = new Task("Создать вторую задачу", "Вот тебе первая таска");
        taskManager.saveTask(newTask);

        Task newTask2 = new Task("Закончить с тасками", "После этого перейди к эпикам");
        taskManager.saveTask(newTask2);

        System.out.println("Список задач: " + taskManager.getAllTasks());
        System.out.println("Список эпиков: " + taskManager.getAllEpics());
        System.out.println("Список подзадач: " + taskManager.getAllSubtasks());

        Epic newEpic = new Epic("Первый эпик", "Эпик для двух подзадач");
        taskManager.saveEpic(newEpic);

        Subtask subtask1 = new Subtask("Первая подзадача", "Для первого эпика", newEpic.getId());
        taskManager.saveSubtask(subtask1);
        Subtask subtask2 = new Subtask("Вторая подзадача", "Для первого же эпика", newEpic.getId());
        taskManager.saveSubtask(subtask2);

        Epic newEpic2 = new Epic("Второй эпик", "Эпик для одной подзадачи");
        taskManager.saveEpic(newEpic2);

        Subtask subtask3 = new Subtask("Третья подзадача", "Для третьего эпика", newEpic2.getId());
        taskManager.saveSubtask(subtask3);

        subtask2.setStatus(Status.DONE);
        taskManager.saveSubtask(subtask2);

        taskManager.deleteAllTasks();
        taskManager.deleteSubtaskById(subtask2.getId());

        System.out.println("Список задач: " + taskManager.getAllTasks());
        System.out.println("Список эпиков: " + taskManager.getAllEpics());
        System.out.println("Список подзадач: " + taskManager.getAllSubtasks());
    }
}
