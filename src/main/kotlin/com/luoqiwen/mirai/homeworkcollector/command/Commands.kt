package com.luoqiwen.mirai.homeworkcollector.command

enum class Commands(val executor: CommandExecutor) {
    submit(SubmitCmdExecutor),
    report(ReportCmdExecutor),
    exam(ExamCmdExecutor),
    notify(NotifyCmdExecutor)
}