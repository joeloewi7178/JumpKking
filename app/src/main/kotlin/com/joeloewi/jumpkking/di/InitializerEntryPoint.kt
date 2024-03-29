/*
 *    Copyright 2022 joeloewi
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.joeloewi.jumpkking.di

import android.app.Application
import android.content.Context
import coil.ImageLoader
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlin.properties.ReadOnlyProperty

@EntryPoint
@InstallIn(SingletonComponent::class)
interface InitializerEntryPoint {
    fun imageLoader(): ImageLoader
    fun application(): Application
}

inline fun <reified EntryPoint> entryPoints() =
    ReadOnlyProperty<Context, EntryPoint> { thisRef, _ ->
        EntryPoints.get(thisRef, EntryPoint::class.java)
    }

val Context.initializerEntryPoint: InitializerEntryPoint by entryPoints()