package com.smartcampus.di

import com.smartcampus.data.dao.AcademicWeeksDao
import com.smartcampus.data.dao.AttendanceDao
import com.smartcampus.data.dao.AuditoriumsDao
import com.smartcampus.data.dao.AuthDao
import com.smartcampus.data.dao.CurriculumsDao
import com.smartcampus.data.dao.GradesDao
import com.smartcampus.data.dao.ScheduleDao
import com.smartcampus.data.dao.SmartCampusProfileDao
import com.smartcampus.data.dao.StudentsDao
import com.smartcampus.data.dao.SubjectsDao
import com.smartcampus.data.dao.SystemAdminDao
import com.smartcampus.data.dao.TeachersDao
import com.smartcampus.data.dao.WorkloadDao
import com.smartcampus.data.dao.WorkloadExecutionDao
import com.smartcampus.data.repositories.AcademicWeeksRepositoryImpl
import com.smartcampus.data.repositories.AttendanceRepositoryImpl
import com.smartcampus.data.repositories.AuditoriumsRepositoryImpl
import com.smartcampus.data.repositories.AuthRepositoryImpl
import com.smartcampus.data.repositories.CurriculumsRepositoryImpl
import com.smartcampus.data.repositories.GradesRepositoryImpl
import com.smartcampus.data.repositories.ScheduleRepositoryImpl
import com.smartcampus.data.repositories.StudentsRepositoryImpl
import com.smartcampus.data.repositories.SubjectsRepositoryImpl
import com.smartcampus.data.repositories.SystemAdminRepositoryImpl
import com.smartcampus.data.repositories.TeachersRepositoryImpl
import com.smartcampus.data.repositories.WorkloadExecutionRepositoryImpl
import com.smartcampus.data.repositories.WorkloadRepositoryImpl
import com.smartcampus.domain.repositories.AcademicWeeksRepository
import com.smartcampus.domain.repositories.AttendanceRepository
import com.smartcampus.domain.repositories.AuditoriumsRepository
import com.smartcampus.domain.repositories.AuthRepository
import com.smartcampus.domain.repositories.CurriculumsRepository
import com.smartcampus.domain.repositories.GradesRepository
import com.smartcampus.domain.repositories.ScheduleRepository
import com.smartcampus.domain.repositories.StudentsRepository
import com.smartcampus.domain.repositories.SubjectsRepository
import com.smartcampus.domain.repositories.SystemAdminRepository
import com.smartcampus.domain.repositories.TeachersRepository
import com.smartcampus.domain.repositories.WorkloadExecutionRepository
import com.smartcampus.domain.repositories.WorkloadRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module


val dataModule = module {

    singleOf(::AcademicWeeksDao)
    singleOf(::AttendanceDao)
    singleOf(::AuditoriumsDao)
    singleOf(::AuthDao)
    singleOf(::CurriculumsDao)
    singleOf(::GradesDao)
    singleOf(::ScheduleDao)
    singleOf(::SmartCampusProfileDao)
    singleOf(::StudentsDao)
    singleOf(::SubjectsDao)
    singleOf(::SystemAdminDao)
    singleOf(::TeachersDao)
    singleOf(::WorkloadDao)
    singleOf(::WorkloadExecutionDao)

    singleOf(::AcademicWeeksRepositoryImpl) bind AcademicWeeksRepository::class
    singleOf(::AttendanceRepositoryImpl) bind AttendanceRepository::class
    singleOf(::AuditoriumsRepositoryImpl) bind AuditoriumsRepository::class
    singleOf(::AuthRepositoryImpl) bind AuthRepository::class
    singleOf(::CurriculumsRepositoryImpl) bind CurriculumsRepository::class
    singleOf(::GradesRepositoryImpl) bind GradesRepository::class
    singleOf(::ScheduleRepositoryImpl) bind ScheduleRepository::class
    singleOf(::StudentsRepositoryImpl) bind StudentsRepository::class
    singleOf(::SubjectsRepositoryImpl) bind SubjectsRepository::class
    singleOf(::SystemAdminRepositoryImpl) bind SystemAdminRepository::class
    singleOf(::TeachersRepositoryImpl) bind TeachersRepository::class
    singleOf(::WorkloadRepositoryImpl) bind WorkloadRepository::class
    singleOf(::WorkloadExecutionRepositoryImpl) bind WorkloadExecutionRepository::class
}
