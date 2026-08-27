package com.comismar.informes.view.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.comismar.informes.view.adapter.MailSender;

public class KeepAliveWorker extends Worker {

    public KeepAliveWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        try {
            MailSender sender = new MailSender(null, null);
            // Enviamos un correo de pulso a la cuenta del sistema
            sender.enviarCorreoKeepAlive(context, "infogenpdf@gmail.com");
            return Result.success();
        } catch (Exception e) {
            AppLogger.logError(context, "KeepAliveWorker", "Fallo crítico en tarea programada", e);
            return Result.failure();
        }
    }
}
