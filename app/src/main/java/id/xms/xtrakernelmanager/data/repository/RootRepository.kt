package id.xms.xtrakernelmanager.data.repository

import com.topjohnwu.superuser.Shell
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RootRepository @Inject constructor() {

    fun isRooted(): Boolean = Shell.getShell().isRoot

    fun checkRootFresh(): Boolean {
        // Execute a command that requires root to verify access is currently available
        return try {
            val result = Shell.cmd("id").exec()
            result.isSuccess && (result.out.any { it.contains("uid=0") || it.contains("root") })
        } catch (e: Exception) {
            false
        }
    }

    fun run(cmd: String): String = Shell.cmd(cmd).exec().out.joinToString("\n")
}